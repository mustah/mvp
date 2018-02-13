import {schema} from 'normalizr';

const measurement = new schema.Entity('measurements');

export const measurementSchema = [measurement];
