import {LegendPayload} from 'recharts';
import {colors} from '../../../app/themes';
import {
  MeasurementResponse,
  MeasurementResponsePart,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary} from '../../../types/Types';
import {Axes, GraphContents} from '../reportModels';

const colorize =
  (colorSchema: {[quantity: string]: string}) =>
    (quantity: Quantity) => colorSchema[quantity as string] || colors.blueA700;

export const colorOf = colorize({
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

const yAxisIdLookup = (axes: Axes, unit: string): 'left' | 'right' | undefined => {
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

const makeLegendPayload = ({average, measurements}: MeasurementResponse): LegendPayload[] => {
  const meterLegends: Dictionary<LegendPayload> = measurements.reduce((prev, {quantity}) => (
    prev[quantity]
      ? prev
      : {
        ...prev,
        [quantity]: {
          type: 'line',
          color: colorOf(quantity),
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
          color: colorOf(quantity),
          value: `Average ${quantity}`,
        },
      }), {});

  const legends: Dictionary<LegendPayload> = {...aggregateLegends, ...meterLegends};

  return Object.keys(legends).map((legend) => legends[legend]);
};

export const toGraphContents =
  (response: MeasurementResponse): GraphContents => {
    const graphContents: GraphContents = {
      axes: {
        left: undefined,
        right: undefined,
      },
      data: [],
      legend: [],
      lines: [],
    };

    const uniqueMeters = new Set<string>();
    const byDate: {[when: number]: {[label: string]: number}} = {};

    let firstTimestamp;

    const {measurements, average} = response;

    measurements.forEach(({id, quantity, label, city, address, medium, values, unit}: MeasurementResponsePart) => {
      const dataKey: string = `${quantity} ${label}`;

      values.forEach(({when, value}) => {
        const created: number = when * 1000;
        if (!firstTimestamp || created < firstTimestamp) {
          firstTimestamp = created;
        }
        if (!byDate[created]) {
          byDate[created] = {};
        }
        byDate[created][dataKey] = value!;
      });

      if (!graphContents.axes.left) {
        graphContents.axes.left = unit;
      } else if (graphContents.axes.left !== unit && !graphContents.axes.right) {
        graphContents.axes.right = unit;
      }

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);

      if (!uniqueMeters.has(dataKey) && yAxisId) {
        uniqueMeters.add(dataKey);

        graphContents.lines.push({
          id,
          dataKey,
          key: dataKey,
          name: label,
          city,
          address,
          medium,
          stroke: colorOf(quantity),
          strokeWidth: 2,
          yAxisId,
        });
      }
    });

    average.forEach(({id, label, quantity, values, unit}: MeasurementResponsePart) => {
      if (!graphContents.axes.left) {
        graphContents.axes.left = unit;
      } else if (graphContents.axes.left !== unit && !graphContents.axes.right) {
        graphContents.axes.right = unit;
      }

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);
      if (yAxisId) {
        const dataKey: string = `Average ${label}`;
        graphContents.lines.push({
          id,
          dataKey,
          key: makeAggregateKey({id, label}),
          name: label,
          stroke: colorOf(quantity),
          strokeWidth: 4,
          yAxisId,
        });
        values.forEach(({when, value}) => {
          const created: number = when * 1000;
          if (created >= firstTimestamp) {
            if (!byDate[created]) {
              byDate[created] = {};
            }
            byDate[created][dataKey] = value!;
          }
        });
      }
    });

    graphContents.data = Object.keys(byDate)
      .map(created => ({...byDate[created], name: Number(created)}))
      .sort(({name: createdA}, {name: createdB}) => createdA - createdB);

    graphContents.legend = makeLegendPayload(response);

    return graphContents;
  };
