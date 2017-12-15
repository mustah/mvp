import {SelectedTabs, TabName} from './tabsModels';

export const getSelectedTab = (state: SelectedTabs): TabName => state.selectedTab;
