import {SET_CURRENT_SELECTION} from '../state/search/selection/selectionActions';
import {Action} from '../types/Types';

export const mockSelectionAction: Action<string> = {type: SET_CURRENT_SELECTION, payload: 'irrelevant'};
