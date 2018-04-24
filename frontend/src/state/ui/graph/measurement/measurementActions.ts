import {createPayloadAction} from 'react-redux-typescript';
import {DateRange, Period} from '../../../../components/dates/dateModels';
import {now, toPeriodApiParameters} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {makeUrl} from '../../../../helpers/urlFactory';
import {EndPoints} from '../../../../services/endPoints';
import {restClient} from '../../../../services/restClient';
import {Dictionary, uuid} from '../../../../types/Types';
import {GraphContents, LineProps, ProprietaryLegendProps} from '../../../../usecases/report/reportModels';
import {
  AverageApiResponse,
  AverageApiResponsePart,
  MeasurementApiResponsePart,
  MeasurementResponses,
  Quantity,
  RenderableQuantity, Resolution,
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

const thickStroke: number = 2;

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

    const legendsMeters: Dictionary<ProprietaryLegendProps> = measurement.reduce((prev, {quantity}) => (
      prev[quantity] ?
        prev
        : {
          ...prev,
          [quantity]: {
            type: 'line',
            color: colorizeMeters(quantity as RenderableQuantity),
            value: quantity,
          },
        }), {});
    const legendsAverage: Dictionary<ProprietaryLegendProps> = average.reduce((prev, {quantity}) => (
      prev[quantity] ?
        prev
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
      if (!uniqueMeters.has(dataKey)) {
        uniqueMeters.add(dataKey);
        const props: LineProps = {
          dataKey,
          key: `line-${dataKey}`,
          name: dataKey,
          stroke: colorizeMeters(quantity as RenderableQuantity),
          strokeWidth: average.length > 0 ? 1 : thickStroke,
        };
        graphContents.lines.push(props);

      }

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
    });

    average.forEach(({quantity, values}: AverageApiResponsePart) => {
      const dataKey: string = `Average ${quantity}`;
      const props: LineProps = {
        dataKey,
        key: `average-${quantity}`,
        name: dataKey,
        stroke: colorizeAverage(quantity as RenderableQuantity),
        strokeWidth: thickStroke,
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
  resolution: Resolution,
): string =>
  `quantities=${quantities.join(',')}` +
  `&meters=${meters.join(',')}` +
  `&${toPeriodApiParameters({now: now(), period: timePeriod, customDateRange}).join('&')}` +
  `&resolution=${resolution}`;

export const fetchMeasurements =
  async (
    quantities: Quantity[],
    selectedListItems: uuid[],
    timePeriod: Period,
    customDateRange: Maybe<DateRange>,
  ): Promise<MeasurementResponses> => {
    let averageData: AverageApiResponse = [];

    if (selectedListItems.length === 0 || quantities.length === 0) {
      return {
        measurement: [],
        average: [],
      };
    }

    if (selectedListItems.length > 1) {
      const averageUrl = makeUrl(
        EndPoints.measurements.concat('/average'),
        measurementUri(quantities, selectedListItems, timePeriod, customDateRange, Resolution.hour),
      );

      try {
        const averageResponse = await restClient.get(averageUrl);
        averageData = averageResponse.data;
      } catch (error) {
        return {
          measurement: [],
          average: [],
        };
      }
    }

    const measurement = makeUrl(
      EndPoints.measurements,
      measurementUri(quantities, selectedListItems, timePeriod, customDateRange, Resolution.hour),
    );
    try {
      const response = await restClient.get(measurement);
      return {
        measurement: response.data,
        average: averageData.map((averageEntity) => ({
          ...averageEntity,
          values: averageEntity.values.filter(({value}) => value),
        })),
      };
    } catch (error) {
      return {
        measurement: [],
        average: [],
      };
    }
  };

export const SAVE_SELECTED_QUANTITIES = 'SAVE_SELECTED_QUANTITIES';

const saveSelectedQuantities = createPayloadAction<string, Quantity[]>(SAVE_SELECTED_QUANTITIES);

export const selectQuantities = (quantities: Quantity[]) => saveSelectedQuantities(quantities);
