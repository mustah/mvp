import {uuid} from '../../../types/Types';

export interface SelectionTreeModel {
  [key: string]: Array<{
    id: uuid;
    name: string;
    selectable: boolean;
    parent: {type: string; id: uuid};
    childNodes: {type: string; ids: uuid[]};
  }>;
}
