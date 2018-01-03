import {AuthState} from './authModels';
import {User} from '../../state/domain-models/user/userModels';

export const getUser = (state: AuthState): User => state.user!;
