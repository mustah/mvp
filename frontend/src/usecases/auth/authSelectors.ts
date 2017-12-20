import {AuthState, User} from './authModels';

export const getUser = (state: AuthState): User => state.user!;
