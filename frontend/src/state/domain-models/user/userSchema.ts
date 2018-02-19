import {schema} from 'normalizr';

const user = new schema.Entity('users');
export const userSchema = [user];

const organisation = new schema.Entity('organisations');
export const organisationSchema = [organisation];
