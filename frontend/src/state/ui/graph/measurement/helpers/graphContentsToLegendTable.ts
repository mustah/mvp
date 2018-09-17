import {normalize, schema} from 'normalizr';
import {uuid} from '../../../../../types/Types';
import {GraphContents, LegendItem, LineProps} from '../../../../../usecases/report/reportModels';
import {Normalized} from '../../../../domain-models/domainModels';

const lineSchema = [new schema.Entity('lines', {}, {idAttribute: 'id'})];

export const graphContentsToLegendTable = ({lines}: GraphContents):  Normalized<LegendItem> => {
  const legendLines: Map<uuid, LegendItem> = new Map<uuid, LegendItem>();

  lines.forEach(({name, address, city, medium, dataKey, id}: LineProps) => {
    legendLines.set(id, {
      label: name,
      address,
      city,
      medium,
      color: '', // TODO a meters lines, should be identifiable by color.
      id,
    });
  });

  return normalize(Array.from(legendLines.values()), lineSchema);
};
