import {SelectedTabs, TabModel, TabName} from './tabsModels';

export const getSelectedTab = (state: SelectedTabs): TabName => state.selectedTab;
export const getTabs = (state: SelectedTabs): TabModel => state.tabs;
