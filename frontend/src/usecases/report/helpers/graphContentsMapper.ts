import {toArray} from 'lodash';
import {LegendPayload} from 'recharts';
import {colors} from '../../../app/themes';
import {
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementValue,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary} from '../../../types/Types';
import {ActivePointPayload, AxesProps, GraphContents} from '../reportModels';

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

interface AggregateKey {
  label: string;
  id: string;
}

const makeAggregateKey = ({label, id}: AggregateKey): string => `aggregate-${label}-${id}`;

const makeLegendPayloads = ({average, measurements}: MeasurementResponse): LegendPayload[] => {
  const meterLegends: Dictionary<LegendPayload> = measurements.reduce((prev, {quantity}) => (
    prev[quantity]
      ? prev
      : {
        ...prev,
        [quantity]: {
          type: 'line',
          color: colorFor(quantity),
          value: quantity,
        },
      }), {});

  const aggregateLegends: Dictionary<LegendPayload> = average.reduce((prev, {id, label, quantity}) => (
    prev[label]
      ? prev
      : {
        ...prev,
        [makeAggregateKey({id, label})]: {
          type: 'line',
          color: colorFor(quantity),
          value: `Average ${quantity}`,
        },
      }), {});

  return toArray({...aggregateLegends, ...meterLegends});
};

const makeAxes = (graphContents: GraphContents, unit: string): void => {
  if (!graphContents.axes.left) {
    graphContents.axes.left = unit;
  } else if (graphContents.axes.left !== unit && !graphContents.axes.right) {
    graphContents.axes.right = unit;
  }
};

export const toGraphContents =
  (response: MeasurementResponse): GraphContents => {
    const graphContents: GraphContents = {
      axes: {left: undefined, right: undefined},
      data: [],
      legend: [],
      lines: [],
    };

    const byDate: {[when: number]: ActivePointPayload} = {};

    let firstTimestamp = Number.MAX_VALUE;

    const {measurements, average} = response;

    const makeByDate = ({when, value, dataKey, timestamp}: MeasurementValue & {dataKey: string, timestamp: number}) => {
      if (!byDate[when]) {
        byDate[when] = {name: Number(when), timestamp};
      }
      byDate[when][dataKey] = value!;
    };

    measurements.forEach(({id, quantity, label, city, address, medium, values, unit}: MeasurementResponsePart) => {
      const dataKey: string = `${quantity} ${label}`;

      makeAxes(graphContents, unit);

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);

      if (yAxisId) {
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

        values.forEach((it) => {
          const timestamp: number = it.when * 1000;
          firstTimestamp = Math.min(firstTimestamp, timestamp);
          makeByDate({...it, when: timestamp, dataKey, timestamp});
        });
      }
    });

    average.forEach(({id, label, quantity, values, unit}: MeasurementResponsePart) => {
      makeAxes(graphContents, unit);

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);
      if (yAxisId) {
        const dataKey: string = `Average ${label}`;
        graphContents.lines.push({
          id,
          dataKey,
          key: makeAggregateKey({id, label}),
          name: label,
          stroke: colorFor(quantity),
          strokeWidth: 4,
          unit,
          yAxisId,
        });
        values.forEach(it => {
          const timestamp: number = it.when * 1000;
          if (timestamp >= firstTimestamp) {
            makeByDate({...it, when: timestamp, dataKey, timestamp});
          }
        });
      }
    });

    graphContents.data = Object.keys(byDate)
      .map(created => ({...byDate[created]}))
      .sort(({name: createdA}, {name: createdB}) => createdA - createdB);

    graphContents.legend = makeLegendPayloads(response);

    return graphContents;
  };
