import {SelectedTabs, TabModel, TopLevelTab} from './tabsModels';

export const getSelectedTab = (state: SelectedTabs): TopLevelTab => state.selectedTab;
export const getTabs = (state: SelectedTabs): TabModel => state.tabs;
