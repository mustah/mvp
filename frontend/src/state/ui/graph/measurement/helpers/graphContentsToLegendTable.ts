import {normalize, schema} from 'normalizr';
import {uuid} from '../../../../../types/Types';
import {GraphContents, LegendItem, LineProps} from '../../../../../usecases/report/reportModels';
import {Normalized} from '../../../../domain-models/domainModels';

const lineSchema = [new schema.Entity('lines', {}, {idAttribute: 'id'})];

export const graphContentsToLegendTable = (graphContents: GraphContents):  Normalized<LegendItem> => {
  const lines: Map<uuid, LegendItem> = new Map<uuid, LegendItem>();

  graphContents.lines.forEach(({name, address, city, medium, dataKey, id}: LineProps) => {
    lines.set(id, {
      label: name,
      address,
      city,
      medium,
      color: '', // TODO a meters lines, should be identifiable by color.
      id,
    });
  });

  return normalize(Array.from(lines.values()), lineSchema);
};
