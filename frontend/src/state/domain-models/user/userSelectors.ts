import {ObjectsById} from '../domainModels';
import {User, UserState} from './userModels';

export const getUsersTotal = (state: UserState): number => state.total;
export const getUserEntities = (state: UserState): ObjectsById<User> => state.entities;
