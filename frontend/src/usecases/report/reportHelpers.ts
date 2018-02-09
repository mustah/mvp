import {unixTimestampMillisecondsToDate} from '../../helpers/formatters';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Measurement} from '../../state/domain-models/measurement/measurementModels';
import {GraphContents, LineProps} from './reportModels';

// TODO this may return undefined
const colorize = (index: number): string => ['#8884d8', '#82ca9d', '#ffb4a4'][index];

export const mapNormalizedPaginatedResultToGraphData = (entities: ObjectsById<Measurement>): GraphContents => {
  const graphContents: GraphContents = {
    axes: {
      left: undefined,
      right: undefined,
    },
    lines: [],
    data: [],
  };

  const byDate = {};
  const meters: string[] = [];

  Object.keys(entities).forEach((entityId: string, index: number) => {
    const entity: Measurement = entities[entityId];

    const created = unixTimestampMillisecondsToDate(entity.created);
    if (!byDate[created]) {
      byDate[created] = {};
    }
    const label: string = entity.id.toString();
    if (!meters.includes(label)) {
      meters.push(label);
      const props: LineProps = {
        stroke: colorize(index),
        key: `line-${label}`,
        name: label,
        dataKey: label,
      };
      graphContents.lines.push(props);
    }

    byDate[created][label] = entity.value;

    if (!graphContents.axes.left) {
      graphContents.axes.left = entity.unit;
    } else if (graphContents.axes.left !== entity.unit && !graphContents.axes.right) {
      graphContents.axes.right = entity.unit;
    }
  });

  graphContents.data = Object.keys(byDate).reduce((acc: any[], current) => {
    const allValuesForDate = byDate[current];
    acc.push({
      ...allValuesForDate,
      name: current,
    });
    return acc;
  }, []);

  return graphContents;
};
