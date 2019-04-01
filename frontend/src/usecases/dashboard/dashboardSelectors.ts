import {WidgetState} from '../../state/widget/widgetReducer';
import {uuid} from '../../types/Types';

export const getMeterCount = (data: WidgetState, id: uuid): number => {
  if (data[id] !== undefined && data[id].data !== undefined) {
    return data[id].data || 0;
  }
  return 0;
};
