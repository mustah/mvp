import {User} from '../../state/domain-models/user/userModels';
import {uuid} from '../../types/Types';
import {AuthState} from './authModels';

export const getUser = (state: AuthState): User => state.user!;
export const getOrganisationSlug = (state: AuthState): uuid => getUser(state).organisation.slug;
