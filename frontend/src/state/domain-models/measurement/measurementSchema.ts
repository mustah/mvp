import {schema} from 'normalizr';

const measurement = new schema.Entity('measurements');
const test = new schema.Entity('tests');

export const measurementSchema = {content: [measurement], test: [test]};
