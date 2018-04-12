import {combineReducers} from 'redux';
import {sideMenu, SideMenuState} from '../../usecases/sidemenu/sideMenuReducer';
import {MeasurementState} from './graph/measurement/measurementModels';
import {measurements} from './graph/measurement/measurementReducer';
import {indicator, IndicatorState} from './indicator/indicatorReducer';
import {MessageState} from './message/messageModels';
import {message} from './message/messageReducer';
import {PaginationState} from './pagination/paginationModels';
import {pagination} from './pagination/paginationReducer';
import {SelectionTreeState} from './selection-tree/selectionTreeModels';
import {selectionTree} from './selection-tree/selectionTreeReducer';
import {TabsState} from './tabs/tabsModels';
import {tabs} from './tabs/tabsReducer';

export interface UiState {
  indicator: IndicatorState;
  measurements: MeasurementState;
  message: MessageState;
  pagination: PaginationState;
  selectionTree: SelectionTreeState;
  sideMenu: SideMenuState;
  tabs: TabsState;
}

export const ui = combineReducers<UiState>({
  indicator,
  measurements,
  message,
  pagination,
  selectionTree,
  sideMenu,
  tabs,
});
