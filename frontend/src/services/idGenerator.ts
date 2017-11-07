import {v4} from 'uuid';
import {uuid} from '../types/Types';

export const idGenerator = {
  uuid(): uuid {
    return v4();
  },
};
