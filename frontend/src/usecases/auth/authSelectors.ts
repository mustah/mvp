import {Organisation} from '../../state/domain-models/organisation/organisationModels';
import {User} from '../../state/domain-models/user/userModels';
import {AuthState} from './authModels';

export const getUser = (state: AuthState): User => state.user!;
export const getOrganisation = (state: AuthState): Organisation => getUser(state).organisation;
export const getOrganisationSlug = (state: AuthState): string => getOrganisation(state).slug;
