import {RESET_SELECTION} from '../state/user-selection/userSelectionActions';
import {Action} from '../types/Types';

export const mockSelectionAction: Action<string> = {type: RESET_SELECTION, payload: 'irrelevant'};
