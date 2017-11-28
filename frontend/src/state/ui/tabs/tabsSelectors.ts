import {SelectedTabs, TabModel, TopLevelTab} from '../../../components/tabs/models/TabsModel';

export const getSelectedTab = (state: SelectedTabs): TopLevelTab => state.selectedTab;
export const getTabs = (state: SelectedTabs): TabModel => state.tabs;
