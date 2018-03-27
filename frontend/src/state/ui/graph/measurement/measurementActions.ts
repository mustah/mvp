import {createPayloadAction} from 'react-redux-typescript';
import {Period} from '../../../../components/dates/dateModels';
import {currentDateRange, toApiParameters} from '../../../../helpers/dateHelpers';
import {makeUrl} from '../../../../helpers/urlFactory';
import {EndPoints} from '../../../../services/endPoints';
import {restClient} from '../../../../services/restClient';
import {firstUpperTranslated} from '../../../../services/translationService';
import {uuid} from '../../../../types/Types';
import {RenderableQuantity} from '../../../../usecases/report/reportHelpers';
import {GraphContents, LineProps} from '../../../../usecases/report/reportModels';
import {AverageApiResponse, MeasurementResponses, Quantity} from './measurementModels';

const colorize =
  (colorSchema: {[quantity: string]: string}) =>
    (quantity: RenderableQuantity) =>
      colorSchema[quantity as string];

const colorizeAverage = colorize({
  [RenderableQuantity.volume as string]: '#3d37ae',
  [RenderableQuantity.flow as string]: '#3d8f5c',
  [RenderableQuantity.energy as string]: 'pink',
  [RenderableQuantity.power as string]: 'pink',
  [RenderableQuantity.forwardTemperature as string]: 'pink',
  [RenderableQuantity.returnTemperature as string]: 'pink',
  [RenderableQuantity.differenceTemperature as string]: 'pink',
});

const defaultColor = '#006da3';
const colorizeMeters = colorize({
  [RenderableQuantity.volume as string]: defaultColor,
  [RenderableQuantity.flow as string]: defaultColor,
  [RenderableQuantity.energy as string]: defaultColor,
  [RenderableQuantity.power as string]: defaultColor,
  [RenderableQuantity.forwardTemperature as string]: defaultColor,
  [RenderableQuantity.returnTemperature as string]: defaultColor,
  [RenderableQuantity.differenceTemperature as string]: defaultColor,
});

const thickStroke: number = 3;

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

    const byDate = {};
    const uniqueMeters = new Set<string>();

    measurement.forEach((meterQuantity, index: number) => {
      const label: string = meterQuantity.quantity + ': ' + meterQuantity.label;
      if (!uniqueMeters.has(label)) {
        uniqueMeters.add(label);
        const props: LineProps = {
          dataKey: label,
          key: `line-${label}`,
          name: label,
          stroke: colorizeMeters(meterQuantity.quantity as RenderableQuantity),
          strokeWidth: average.length > 0 ? 1 : thickStroke,
        };
        graphContents.lines.push(props);
      }

      meterQuantity.values.forEach(({when, value}) => {
        const created = when * 1000;
        if (!byDate[created]) {
          byDate[created] = {};
        }
        byDate[created][label] = value;
      });

      if (!graphContents.axes.left) {
        graphContents.axes.left = meterQuantity.unit;
      } else if (graphContents.axes.left !== meterQuantity.unit && !graphContents.axes.right) {
        graphContents.axes.right = meterQuantity.unit;
      }
    });

    average.forEach((averageQuantity, index: number) => {
      const label: string = firstUpperTranslated('average') + ' - ' + averageQuantity.quantity;
      const props: LineProps = {
        dataKey: label,
        key: `average-${averageQuantity.quantity}`,
        name: `Average ${averageQuantity.quantity}`,
        stroke: colorizeAverage(averageQuantity.quantity as RenderableQuantity),
        strokeWidth: thickStroke,
      };
      graphContents.lines.push(props);

      averageQuantity.values.forEach(({when, value}) => {
        if (value === null) {
          return;
        }
        const created = when * 1000;
        if (!byDate[created]) {
          byDate[created] = {};
        }
        byDate[created][label] = value;
      });
    });

    graphContents.data = Object.keys(byDate).reduce((acc: any[], label) => {
      const allValuesForDate = byDate[label];
      acc.push({
        ...allValuesForDate,
        name: Number(label),
      });
      return acc;
    }, []);

    return graphContents;
  };

const measurementUri = (quantities: Quantity[], meters: uuid[], timePeriod: Period): string =>
  `quantities=${quantities.join(',')}` +
  `&meters=${meters.join(',')}` +
  `&${toApiParameters(currentDateRange(timePeriod)).join('&')}`;

export const fetchMeasurements =
  async (quantities: Quantity[], selectedListItems: uuid[], timePeriod: Period): Promise<MeasurementResponses> => {
    let averageData: AverageApiResponse = [];

    if (selectedListItems.length === 0 || quantities.length === 0) {
      return {
        measurement: [],
        average: [],
      };
    }

    if (selectedListItems.length > 1) {
      // TODO make sure timePeriod is not a Maybe, coming in Must's future MR
      const averageUrl = makeUrl(
        EndPoints.measurements.concat('/average'),
        measurementUri(quantities, selectedListItems, timePeriod),
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

    const measurement = makeUrl(EndPoints.measurements, measurementUri(quantities, selectedListItems, timePeriod));
    try {
      const response = await restClient.get(measurement);
      return {
        measurement: response.data,
        average: averageData,
      };
    } catch (error) {
      return {
        measurement: [],
        average: [],
      };
    }
  };

export const SAVE_SELECTED_QUANTITIES = 'SAVE_SELECTED_QUANTITIES';

const saveSelectedQuantites = createPayloadAction<string, Quantity[]>(SAVE_SELECTED_QUANTITIES);

export const selectQuantities =
  (quantities: Quantity[]) => saveSelectedQuantites(quantities);
