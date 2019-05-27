import {UiState} from '../uiReducer';
import {TabName} from './tabsModels';

export const getSelectedTab = (state: UiState): TabName => state.tabs.validation.selectedTab;
