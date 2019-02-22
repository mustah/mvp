import {Dictionary} from '../../../../types/Types';
import {Axes, GraphContents, ProprietaryLegendProps} from '../../../../usecases/report/reportModels';
import {AverageResponsePart, MeasurementResponse, MeasurementResponsePart, Quantity} from './measurementModels';

const colorize =
  (colorSchema: {[quantity: string]: string}) =>
    (quantity: Quantity) =>
      colorSchema[quantity as string];

const colorizeAverage = colorize({
  [Quantity.volume as string]: '#5555ff',
  [Quantity.flow as string]: '#ff99ff',
  [Quantity.energy as string]: '#439c43',
  [Quantity.power as string]: '#00aaaa',
  [Quantity.forwardTemperature as string]: '#843939',
  [Quantity.returnTemperature as string]: '#a7317d',
  [Quantity.differenceTemperature as string]: '#004d78',
});

export const colorizeMeters = colorize({
  [Quantity.volume as string]: '#651FFF',
  [Quantity.flow as string]: '#F50057',
  [Quantity.energy as string]: '#00E676',
  [Quantity.power as string]: '#00B0FF',
  [Quantity.forwardTemperature as string]: '#FF1744',
  [Quantity.returnTemperature as string]: '#D500F9',
  [Quantity.differenceTemperature as string]: '#2979FF',
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

export const toGraphContents =
  ({measurements, average}: MeasurementResponse): GraphContents => {
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

    const legendsMeters: Dictionary<ProprietaryLegendProps> = measurements.reduce((
      prev,
      {quantity},
    ) => (
      prev[quantity]
        ? prev
        : {
          ...prev,
          [quantity]: {
            type: 'line',
            color: colorizeMeters(quantity as Quantity),
            value: quantity,
          },
        }), {});

    const legendsAverage: Dictionary<ProprietaryLegendProps> = average.reduce((
      prev,
      {quantity},
    ) => (
      prev[quantity]
        ? prev
        : {
          ...prev,
          [`average-${quantity}`]: {
            type: 'line',
            color: colorizeAverage(quantity as Quantity),
            value: `Average ${quantity}`,
          },
        }), {});

    const legends: Dictionary<ProprietaryLegendProps> = {...legendsMeters, ...legendsAverage};

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
        // we should already have filtered out missing values
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
          stroke: colorizeMeters(quantity as Quantity),
          strokeWidth: 3,
          yAxisId,
          origin: 'meter',
        });
      }
    });

    average.forEach(({id, quantity, values, unit}: AverageResponsePart) => {
      if (!graphContents.axes.left) {
        graphContents.axes.left = unit;
      } else if (graphContents.axes.left !== unit && !graphContents.axes.right) {
        graphContents.axes.right = unit;
      }

      const yAxisId = yAxisIdLookup(graphContents.axes, unit);
      if (!yAxisId) {
        return;
      }
      const dataKey: string = `Average ${quantity}`;
      graphContents.lines.push({
        id,
        dataKey,
        key: `average-${quantity}`,
        name: dataKey,
        stroke: colorizeAverage(quantity as Quantity),
        strokeWidth: 4,
        yAxisId,
        origin: 'average',
      });

      values.forEach(({when, value}) => {
        const created: number = when * 1000;
        if (created < firstTimestamp) {
          return;
        }
        if (!byDate[created]) {
          byDate[created] = {};
        }
        // we should already have filtered out missing values
        byDate[created][dataKey] = value!;
      });
    });

    graphContents.data = Object.keys(byDate).map((created) => ({
      ...byDate[created],
      name: Number(created),
    })).sort(({name: createdA}, {name: createdB}) => createdA - createdB);

    graphContents.legend = Object.keys(legends).map((legend) => legends[legend]);
    return graphContents;
  };
