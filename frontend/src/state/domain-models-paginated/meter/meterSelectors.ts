import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {NormalizedPaginatedState} from '../paginatedDomainModels';
import {Meter} from './meterModels';

export const getMeter = ({entities}: NormalizedPaginatedState<Meter>, id: uuid): Maybe<Meter> =>
  Maybe.maybe(entities[id]);
