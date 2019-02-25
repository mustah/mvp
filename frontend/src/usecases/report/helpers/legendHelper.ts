import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {getMediumType} from '../../../state/ui/graph/measurement/measurementModels';
import {LegendItem} from '../reportModels';

export const toLegendItem = ({id, facility, medium}: Meter): LegendItem =>
  ({id, label: facility as string, medium: getMediumType(medium)});
