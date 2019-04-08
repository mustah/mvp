import {User} from '../../state/domain-models/user/userModels';
import {AuthState} from './authModels';

export const getUser = (state: AuthState): User => state.user!;
export const getOrganisationSlug = (state: AuthState): string => getUser(state).organisation.slug;
