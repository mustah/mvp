import {RESET_SELECTION} from '../state/search/selection/selectionActions';
import {Action} from '../types/Types';

export const mockSelectionAction: Action<string> = {type: RESET_SELECTION, payload: 'irrelevant'};
