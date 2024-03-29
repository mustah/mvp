import {sortBy, toArray, uniqBy} from 'lodash';
import {LegendPayload, ReferenceLineProps} from 'recharts';
import {colors} from '../../../app/colors';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {AxesProps, GraphContents} from '../../../state/report/reportModels';
import {
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementValue,
  Quantity,
  TooltipMeta
} from '../../../state/ui/graph/measurement/measurementModels';
import {ThresholdQuery} from '../../../state/user-selection/userSelectionModels';
import {Dictionary, Predicate} from '../../../types/Types';

const colorize =
  (colorSchema: {[key: string]: string}) =>
    (key: string) => colorSchema[key as string] || '#00B0FF';

export const colorFor = colorize({
  [Quantity.volume as string]: '#651FFF',
  [Quantity.flow as string]: '#F50057',
  [Quantity.energy as string]: '#00E676',
  [Quantity.power as string]: '#00B0FF',
  [Quantity.forwardTemperature as string]: '#FF1744',
  [Quantity.returnTemperature as string]: '#D500F9',
  [Quantity.differenceTemperature as string]: '#2979FF',
  [Quantity.externalTemperature as string]: '#e84d6f',
  [Quantity.relativeHumidity as string]: '#ff9800',
  ['count']: '#b6e2cc',
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
  name?: string;
  label: string;
  meterAddress?: string;
  quantity: Quantity;
}

export interface ByDate {
  [when: number]: Dictionary<number | any>;
}

const makeDataKey = ({id, label, quantity}: DataKey): string => `${quantity}-${label}-${id}`;

export const makeAggregateKey = (key: DataKey): string => `aggregate-${makeDataKey(key)}`;
export const makeMeasurementKey = (key: DataKey): string => `measurement-${makeDataKey(key)}`;
export const makeCompareKey = (key: DataKey): string => `compare-${makeDataKey(key)}`;
export const makeTimestampKey = (dataKey: string): string => `${dataKey}-timestamp`;
export const makeMetaKey = (dataKey: string): string => `${dataKey}-meta`;

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

const makeLegendPayloads = (
  {average, measurements}: MeasurementResponse,
  justSelectedQuantities: Predicate<MeasurementResponsePart>,
): LegendPayload[] => {
  const aggregateLegends: Dictionary<LegendPayload> = average.filter(justSelectedQuantities)
    .reduce(legendPayloadReducer(
      ({id, label, quantity}) => makeAggregateKey({id, label, quantity}),
      ({quantity}) => `${firstUpperTranslated('average')} ${quantity}`,
    ), {});

  const meterLegends: Dictionary<LegendPayload> = measurements.filter(justSelectedQuantities)
    .reduce(legendPayloadReducer(
      ({quantity}) => quantity,
      ({quantity}) => quantity,
    ), {});

  return uniqBy(toArray({...meterLegends, ...aggregateLegends}), 'value');
};

const makeAxes = (graphContents: GraphContents, unit: string): void => {
  if (!graphContents.axes.left) {
    graphContents.axes.left = unit;
  } else if (graphContents.axes.left !== unit && !graphContents.axes.right) {
    graphContents.axes.right = unit;
  }
};

interface Meta {
  dataKey: string;
  timestamp?: number;
}

type TooltipContentProps = MeasurementValue & Meta & TooltipMeta;

export const toGraphContents =
  (response: MeasurementResponse, selectedQuantities: Quantity[]): GraphContents => {
    const graphContents: GraphContents = {
      axes: {},
      data: [],
      legend: [],
      lines: [],
    };

    const byDate: ByDate = {};

    const {average, measurements, compare} = response;

    const makeByDate = ({id, quantity, when, value, dataKey, timestamp}: TooltipContentProps) => {
      let payloadItem = byDate[when];
      if (!payloadItem) {
        payloadItem = {name: Number(when)};
      }
      byDate[when] = {
        ...payloadItem,
        [dataKey]: value!,
        [makeTimestampKey(dataKey)]: Number(timestamp),
        [makeMetaKey(dataKey)]: {id, quantity},
      };
    };

    const sortedMeasurementValues: Dictionary<MeasurementValue[]> = {};

    const justSelectedQuantities = (it: MeasurementResponsePart) => selectedQuantities.indexOf(it.quantity) !== -1;

    measurements.filter(justSelectedQuantities)
      .forEach(({id, quantity, label, values, unit}: MeasurementResponsePart) => {
        makeAxes(graphContents, unit);

        const yAxisId = yAxisIdLookup(graphContents.axes, unit);

        if (yAxisId) {
          const dataKey: string = makeMeasurementKey({id, label, quantity});
          graphContents.lines.push({
            id,
            dataKey,
            key: dataKey,
            name: label,
            stroke: colorFor(quantity),
            strokeWidth: 1,
            unit,
            yAxisId,
          });

          values.forEach(it => {
            const timestamp: number = it.when * 1000;
            makeByDate({...it, when: timestamp, dataKey, id, quantity});
          });
          sortedMeasurementValues[dataKey] = sortBy(values, it => it.when);
        }
      });

    compare.forEach(({id, label, quantity, unit, values}: MeasurementResponsePart) => {
      const yAxisId = yAxisIdLookup(graphContents.axes, unit);

      if (yAxisId) {
        const dataKey: string = makeCompareKey({id, label, quantity});
        graphContents.lines.push({
          id,
          dataKey,
          key: dataKey,
          name: label,
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
            const sortedMeasurementValue = sortedMeasurementValues[measurementKey];
            if (sortedMeasurementValue) {
              const measurement = sortedMeasurementValue[index];
              if (measurement) {
                const when = measurement.when * 1000;
                makeByDate({when, value: it.value!, dataKey, timestamp, id, quantity});
              }
            }
          });
      }
    });

    average.filter(justSelectedQuantities)
      .forEach(({id, label, quantity, values, unit}: MeasurementResponsePart) => {
        makeAxes(graphContents, unit);

        const yAxisId = yAxisIdLookup(graphContents.axes, unit);
        if (yAxisId) {
          const dataKey: string = makeAggregateKey({id, label, quantity});
          graphContents.lines.push({
            id,
            dataKey,
            key: dataKey,
            name: `${firstUpperTranslated('average')}: ${label}`,
            stroke: colorFor(quantity),
            strokeWidth: 4,
            unit,
            yAxisId,
          });
          values.forEach(it => {
            const timestamp: number = it.when * 1000;
            makeByDate({...it, when: timestamp, dataKey, id, quantity});
          });
        }
      });

    graphContents.data = Object.keys(byDate)
      .map(created => ({...byDate[created]}))
      .sort(({name: createdA}, {name: createdB}) => createdA - createdB);

    graphContents.legend = makeLegendPayloads(response, justSelectedQuantities);

    return graphContents;
  };

export const toReferenceLineProps = ({left}: AxesProps, threshold?: ThresholdQuery): ReferenceLineProps | undefined =>
  Maybe.maybe(threshold)
    .map(({unit, value}: ThresholdQuery): ReferenceLineProps => ({
      label: `${firstUpperTranslated('threshold')} ${value} ${unit}`,
      stroke: colors.thresholdStroke,
      strokeWidth: 2,
      y: value,
      yAxisId: left ? 'left' : 'right',
    }))
    .getOrElseUndefined();
