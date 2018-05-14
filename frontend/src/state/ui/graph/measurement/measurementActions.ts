import {AxiosPromise, AxiosResponse} from 'axios';
import {createPayloadAction} from 'react-redux-typescript';
import {DateRange, Period} from '../../../../components/dates/dateModels';
import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {now, toPeriodApiParameters} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {makeUrl} from '../../../../helpers/urlFactory';
import {EndPoints} from '../../../../services/endPoints';
import {restClient, wasRequestCanceled} from '../../../../services/restClient';
import {firstUpperTranslated} from '../../../../services/translationService';
import {Dictionary, EncodedUriParameters, ErrorResponse, uuid} from '../../../../types/Types';
import {OnLogout} from '../../../../usecases/auth/authModels';
import {OnUpdateGraph} from '../../../../usecases/report/containers/GraphContainer';
import {Axes, GraphContents, LineProps, ProprietaryLegendProps} from '../../../../usecases/report/reportModels';
import {
  emptyGraphContents,
  MeasurementApiResponse,
  MeasurementApiResponsePart,
  MeasurementResponses,
  Quantity,
  RenderableQuantity,
} from './measurementModels';

const colorize =
  (colorSchema: {[quantity: string]: string}) =>
    (quantity: RenderableQuantity) =>
      colorSchema[quantity as string];

const colorizeAverage = colorize({
  [RenderableQuantity.volume as string]: '#5555ff',
  [RenderableQuantity.flow as string]: '#ff99ff',
  [RenderableQuantity.energy as string]: '#439c43',
  [RenderableQuantity.power as string]: '#00aaaa',
  [RenderableQuantity.forwardTemperature as string]: '#843939',
  [RenderableQuantity.returnTemperature as string]: '#a7317d',
  [RenderableQuantity.differenceTemperature as string]: '#004d78',
});

const colorizeMeters = colorize({
  [RenderableQuantity.volume as string]: '#0000ff',
  [RenderableQuantity.flow as string]: '#ff00ff',
  [RenderableQuantity.energy as string]: '#00ff00',
  [RenderableQuantity.power as string]: '#00ffff',
  [RenderableQuantity.forwardTemperature as string]: '#ff0000',
  [RenderableQuantity.returnTemperature as string]: '#ff49bd',
  [RenderableQuantity.differenceTemperature as string]: '#0084e6',
});

const thickStroke: number = 4;

const yAxisIdLookup = (axes: Axes, unit: string): 'left' | 'right' | undefined => {
  if (axes.left === unit) {
    return 'left';
  }
  if (axes.right === unit) {
    return 'right';
  }
  return undefined;
};

export const mapApiResponseToGraphData =
  ({measurement, average}: MeasurementResponses): GraphContents => {
    const graphContents: GraphContents = {
      axes: {
        left: undefined,
        right: undefined,
      },
      data: [],
      legend: [],
      lines: [],
    };

    const byDate: {[when: number]: {[label: string]: number}} = {};
    const uniqueMeters = new Set<string>();
    let firstTimestamp;

    const legendsMeters: Dictionary<ProprietaryLegendProps> = measurement.reduce((
      prev,
      {quantity},
    ) => (
      prev[quantity] ? prev
        : {
          ...prev,
          [quantity]: {
            type: 'line',
            color: colorizeMeters(quantity as RenderableQuantity),
            value: quantity,
          },
        }), {});

    const legendsAverage: Dictionary<ProprietaryLegendProps> = average.reduce((
      prev,
      {quantity},
    ) => (
      prev[quantity] ? prev
        : {
          ...prev,
          [`average-${quantity}`]: {
            type: 'line',
            color: colorizeAverage(quantity as RenderableQuantity),
            value: `Average ${quantity}`,
          },
        }), {});

    const legends: Dictionary<ProprietaryLegendProps> = {...legendsMeters, ...legendsAverage};

    measurement.forEach(({quantity, label, values, unit}: MeasurementApiResponsePart) => {
      const dataKey: string = `${quantity} ${label}`;

      values.forEach(({when, value}) => {
        const created: number = when * 1000;
        if (!firstTimestamp || created < firstTimestamp) {
          firstTimestamp = created;
        }
        if (!byDate[created]) {
          byDate[created] = {};
        }
        byDate[created][dataKey] = value;
      });

      if (!graphContents.axes.left) {
        graphContents.axes.left = unit;
      } else if (graphContents.axes.left !== unit && !graphContents.axes.right) {
        graphContents.axes.right = unit;
      }

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);

      if (!uniqueMeters.has(dataKey) && yAxisId) {
        uniqueMeters.add(dataKey);
        const props: LineProps = {
          dataKey,
          key: `line-${dataKey}`,
          name: dataKey,
          stroke: colorizeMeters(quantity as RenderableQuantity),
          strokeWidth: average.length > 0 ? 1 : thickStroke,
          yAxisId,
        };
        graphContents.lines.push(props);
      }
    });

    average.forEach(({quantity, values, unit}: MeasurementApiResponsePart) => {
      const yAxisId = yAxisIdLookup(graphContents.axes, unit);
      if (!yAxisId) {
        return;
      }
      const dataKey: string = `Average ${quantity}`;
      const props: LineProps = {
        dataKey,
        key: `average-${quantity}`,
        name: dataKey,
        stroke: colorizeAverage(quantity as RenderableQuantity),
        strokeWidth: thickStroke,
        yAxisId,
      };
      graphContents.lines.push(props);

      values.forEach(({when, value}) => {
        const created: number = when * 1000;
        if (created < firstTimestamp) {
          return;
        }
        if (!byDate[created]) {
          byDate[created] = {};
        }
        byDate[created][dataKey] = value;
      });
    });

    graphContents.data = Object.keys(byDate).map((created) => ({
      ...byDate[created],
      name: Number(created),
    })).sort(({name: createdA}, {name: createdB}) => createdA - createdB);

    graphContents.legend = Object.keys(legends).map((legend) => legends[legend]);
    return graphContents;
  };

const measurementUri = (
  quantities: Quantity[],
  meters: uuid[],
  timePeriod: Period,
  customDateRange: Maybe<DateRange>,
): string =>
  `quantities=${quantities.join(',')}` +
  `&meters=${meters.join(',')}` +
  `&${toPeriodApiParameters({now: now(), period: timePeriod, customDateRange}).join('&')}`;

interface GraphDataResponse {
  data: MeasurementApiResponse;
}

export const fetchMeasurements =
  async (
    selectedIndicators: Medium[],
    quantities: Quantity[],
    selectedListItems: uuid[],
    timePeriod: Period,
    customDateRange: Maybe<DateRange>,
    updateState: OnUpdateGraph,
    logout: OnLogout,
  ): Promise<void> => {

    if (selectedIndicators.length === 0 || selectedListItems.length === 0 || quantities.length === 0) {
      updateState({
        graphContents: emptyGraphContents,
        isFetching: false,
        error: Maybe.nothing(),
      });
      return;
    }

    const averageUrl: EncodedUriParameters = makeUrl(
      EndPoints.measurements.concat('/average'),
      measurementUri(quantities, selectedListItems, timePeriod, customDateRange),
    );
    const averageRequest: () => Promise<GraphDataResponse> =
      selectedListItems.length > 1
        ? () => restClient.get(averageUrl)
        : () => new Promise<GraphDataResponse>((resolve) => resolve({data: []}));

    const measurementUrl: EncodedUriParameters = makeUrl(
      EndPoints.measurements,
      measurementUri(quantities, selectedListItems, timePeriod, customDateRange),
    );
    const measurementsRequest = (): AxiosPromise<MeasurementApiResponse> => restClient.get(measurementUrl);

    try {
      const response: [AxiosResponse<MeasurementApiResponse>, GraphDataResponse] =
        await Promise.all([measurementsRequest(), averageRequest()]);

      const graphData: MeasurementResponses = {
        measurement: response[0].data,
        average: response[1].data.map((averageEntity) => ({
          ...averageEntity,
          values: averageEntity.values.filter(({value}) => value),
        })),
      };

      updateState({
        graphContents: mapApiResponseToGraphData(graphData),
        isFetching: false,
        error: Maybe.nothing(),
      });
    } catch (error) {
      if (error instanceof InvalidToken) {
        await logout(error);
      } else if (wasRequestCanceled(error)) {
        return;
      } else {
        const {response} = error;
        const data: ErrorResponse = response && response.data ||
          {message: firstUpperTranslated('an unexpected error occurred')};
        updateState({
          graphContents: emptyGraphContents,
          isFetching: false,
          error: Maybe.maybe(data),
        });
      }
    }

  };

export const SAVE_SELECTED_QUANTITIES = 'SAVE_SELECTED_QUANTITIES';

const saveSelectedQuantities = createPayloadAction<string, Quantity[]>(SAVE_SELECTED_QUANTITIES);

export const selectQuantities = (quantities: Quantity[]) => saveSelectedQuantities(quantities);
