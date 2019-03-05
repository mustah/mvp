import {sortBy, toArray} from 'lodash';
import {LegendPayload} from 'recharts';
import {colors} from '../../../app/themes';
import {firstUpperTranslated} from '../../../services/translationService';
import {
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementValue,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary} from '../../../types/Types';
import {AxesProps, GraphContents} from '../reportModels';

const colorize =
  (colorSchema: {[key: string]: string}) =>
    (key: string) => colorSchema[key as string] || colors.blueA700;

export const colorFor = colorize({
  [Quantity.volume as string]: '#651FFF',
  [Quantity.flow as string]: '#F50057',
  [Quantity.energy as string]: '#00E676',
  [Quantity.power as string]: '#00B0FF',
  [Quantity.forwardTemperature as string]: '#FF1744',
  [Quantity.returnTemperature as string]: '#D500F9',
  [Quantity.differenceTemperature as string]: '#2979FF',
  [Quantity.externalTemperature as string]: colors.red,
  [Quantity.relativeHumidity as string]: colors.orange,
});

const yAxisIdLookup = (axes: AxesProps, unit: string): 'left' | 'right' | undefined => {
  if (axes.left === unit) {
    return 'left';
  }
  if (axes.right === unit) {
    return 'right';
  }
  return undefined;
};

interface DataKey {
  id: string;
  label: string;
  quantity: Quantity;
}

export interface ByDate {
  [when: number]: Dictionary<number>;
}

const makeDataKey = ({id, label, quantity}: DataKey): string => `${quantity}-${label}-${id}`;

export const makeAggregateKey = (key: DataKey): string => `aggregate-${makeDataKey(key)}`;
export const makeMeasurementKey = (key: DataKey): string => `measurement-${makeDataKey(key)}`;
export const makeCompareKey = (key: DataKey): string => `compare-${makeDataKey(key)}`;
export const makeTimestampKey = (dataKey: string): string => `${dataKey}-timestamp`;

type KeyFactory = (response: MeasurementResponsePart) => string;
type ValueFactory = (response: MeasurementResponsePart) => string;

const legendPayloadReducer = (keyFactory: KeyFactory, valueFactory: ValueFactory) =>
  (prev, responsePart: MeasurementResponsePart) => (
    prev[keyFactory(responsePart)]
      ? prev
      : {
        ...prev,
        [keyFactory(responsePart)]: {
          type: 'line',
          color: colorFor(responsePart.quantity),
          value: valueFactory(responsePart),
        },
      });

const makeLegendPayloads = ({average, measurements}: MeasurementResponse): LegendPayload[] => {
  const aggregateLegends: Dictionary<LegendPayload> =
    average.reduce(legendPayloadReducer(
      ({id, label, quantity}) => makeAggregateKey({id, label, quantity}),
      ({id, label, quantity}) => `${firstUpperTranslated('average')} ${quantity}`,
    ), {});

  const meterLegends: Dictionary<LegendPayload> = measurements.reduce(legendPayloadReducer(
    ({quantity}) => quantity,
    ({quantity}) => quantity,
  ), {});

  return toArray({...meterLegends, ...aggregateLegends});
};

const makeAxes = (graphContents: GraphContents, unit: string): void => {
  if (!graphContents.axes.left) {
    graphContents.axes.left = unit;
  } else if (graphContents.axes.left !== unit && !graphContents.axes.right) {
    graphContents.axes.right = unit;
  }
};

type Created = MeasurementValue & {dataKey: string, timestamp?: number};

export const toGraphContents =
  (response: MeasurementResponse): GraphContents => {
    const graphContents: GraphContents = {
      axes: {},
      data: [],
      legend: [],
      lines: [],
    };

    const byDate: ByDate = {};

    const {average, measurements, compare} = response;

    const makeByDate = ({when, value, dataKey, timestamp}: Created) => {
      let payloadItem = byDate[when];
      if (!payloadItem) {
        payloadItem = {name: Number(when)};
      }
      byDate[when] = {...payloadItem, [dataKey]: value!, [makeTimestampKey(dataKey)]: Number(timestamp)};
    };

    const sortedMeasurementValues: Dictionary<MeasurementValue[]> = {};

    measurements.forEach(({id, quantity, label, city, address, medium, values, unit}: MeasurementResponsePart) => {
      makeAxes(graphContents, unit);

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);

      if (yAxisId) {
        const dataKey: string = makeMeasurementKey({id, label, quantity});
        graphContents.lines.push({
          id,
          dataKey,
          key: dataKey,
          name: `${quantity} ${label}`,
          stroke: colorFor(quantity),
          strokeWidth: 1,
          unit,
          yAxisId,
        });

        values.forEach(it => {
          const timestamp: number = it.when * 1000;
          makeByDate({...it, when: timestamp, dataKey});
        });
        sortedMeasurementValues[dataKey] = sortBy(values, it => it.when);
      }
    });

    compare.forEach(({id, label, city, address, medium, quantity, unit, values}: MeasurementResponsePart) => {
      const yAxisId = yAxisIdLookup(graphContents.axes, unit);

      if (yAxisId) {
        const dataKey: string = makeCompareKey({id, label, quantity});
        graphContents.lines.push({
          id,
          dataKey,
          key: dataKey,
          name: `${quantity} ${label}`,
          stroke: colorFor(quantity),
          strokeWidth: 1,
          strokeDasharray: '5 5',
          unit,
          yAxisId,
        });

        const measurementKey = makeMeasurementKey({id, label, quantity});
        sortBy(values, it => it.when)
          .forEach((it, index) => {
            const timestamp: number = it.when * 1000;
            const measurement = sortedMeasurementValues[measurementKey][index];
            if (measurement) {
              makeByDate({when: measurement.when * 1000, value: it.value!, dataKey, timestamp});
            }
          });
      }
    });

    average.forEach(({id, label, quantity, values, unit}: MeasurementResponsePart) => {
      makeAxes(graphContents, unit);

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);
      if (yAxisId) {
        const dataKey: string = makeAggregateKey({id, label, quantity});
        graphContents.lines.push({
          id,
          dataKey,
          key: dataKey,
          name: `${firstUpperTranslated('average')}: ${label} ${quantity}`,
          stroke: colorFor(quantity),
          strokeWidth: 4,
          unit,
          yAxisId,
        });
        values.forEach(it => {
          const timestamp: number = it.when * 1000;
          makeByDate({...it, when: timestamp, dataKey});
        });
      }
    });

    graphContents.data = Object.keys(byDate)
      .map(created => ({...byDate[created]}))
      .sort(({name: createdA}, {name: createdB}) => createdA - createdB);

    graphContents.legend = makeLegendPayloads(response);

    return graphContents;
  };
