import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {NormalizedPaginatedState} from '../paginatedDomainModels';
import {Gateway} from './gatewayModels';

export const getGateway = ({entities}: NormalizedPaginatedState<Gateway>, id: uuid): Maybe<Gateway> =>
  Maybe.maybe(entities[id]);
