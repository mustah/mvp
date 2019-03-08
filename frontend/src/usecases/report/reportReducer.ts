import {combineReducers} from 'redux';
import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {toggle} from '../../helpers/collections';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {Action, uuid} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import {
  addLegendItems,
  removeAllByType,
  showHideAllByType,
  showHideLegendRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByType,
  toggleShowAverage as toggleShowAverageAction,
} from './reportActions';
import {
  LegendItem,
  LegendType,
  LegendViewOptions,
  QuantityId,
  QuantityLegendType,
  ReportState,
  SavedReportsState,
  ViewOptions
} from './reportModels';
import {getLegendItems, getLegendViewOptions, getViewOptions} from './reportSelectors';
import {initialState as initialTemporalState, temporal} from './temporalReducer';

export const makeInitialLegendViewOptions = (): LegendViewOptions =>
  Object.keys(Medium).map(k => Medium[k])
    .reduce((acc, medium) => ({...acc, [medium]: {quantities: []}}), {aggregate: {quantities: []}});

export const initialSavedReportState: SavedReportsState = {
  meterPage: {
    id: 'meterPage',
    legendItems: [],
    legendViewOptions: makeInitialLegendViewOptions(),
    shouldShowAverage: false,
  }
};

export const initialState: ReportState = {
  savedReports: initialSavedReportState,
  temporal: initialTemporalState
};

type ActionTypes =
  | Action<LegendItem[] | uuid | LegendType | QuantityLegendType | QuantityId>
  | EmptyAction<string>;

const getLegendType = (action: ActionTypes): LegendType => (action as Action<LegendType>).payload;

const toggleLegendItemsRows = (state: SavedReportsState, type: LegendType): LegendItem[] =>
  getLegendItems(state).map(it => it.type === type ? {...it, isRowExpanded: !it.isRowExpanded} : it);

const setLegendItems = (state: SavedReportsState, legendItems: LegendItem[]): SavedReportsState => ({
    ...state,
    meterPage: {
      ...state.meterPage,
      legendItems
    }
  }
);

const toggleHiddenLines = (savedReports: SavedReportsState, legendType: LegendType): SavedReportsState => {
  if (savedReports.meterPage.legendItems.filter(it => it.type === legendType).length > 0) {
    const legendViewOptions: LegendViewOptions = savedReports.meterPage.legendViewOptions;
    const viewOptions: ViewOptions = legendViewOptions[legendType];
    return ({
        ...savedReports,
        meterPage: {
          ...savedReports.meterPage,
          legendViewOptions: {
            ...legendViewOptions,
            [legendType]: {...viewOptions, isAllLinesHidden: !viewOptions.isAllLinesHidden}
          }
        }
      }
    );
  }
  return savedReports;
};

const toggleQuantityLegendType = (
  state: SavedReportsState,
  {type, quantity}: QuantityLegendType
): SavedReportsState => {
  const legendViewOptions = getLegendViewOptions(state);
  const viewOptions: ViewOptions = legendViewOptions[type];
  const quantities = toggle(quantity, viewOptions.quantities);
  const legendItems = getLegendItems(state).map(it => it.type === type ? {...it, quantities} : it);
  return {
    ...state,
    meterPage: {
      ...state.meterPage,
      legendItems,
      legendViewOptions: {
        ...legendViewOptions,
        [type]: {...viewOptions, quantities}
      }
    }
  };
};

const removeAllByLegendType = (state: SavedReportsState, type: LegendType): SavedReportsState => {
  const legendViewOptions = state.meterPage.legendViewOptions;
  const legendItems = getLegendItems(state).filter(it => it.type !== type);
  return ({
      ...state,
      meterPage: {
        ...state.meterPage,
        legendItems,
        legendViewOptions: {
          ...legendViewOptions,
          [type]: {quantities: []}
        }
      }
    }
  );
};

const toggleQuantityId = (state: SavedReportsState, {id, quantity}: QuantityId): SavedReportsState => {
  const meters = getLegendItems(state)
    .map(it => it.id === id ? {...it, quantities: toggle(quantity, it.quantities)} : it);
  return setLegendItems(state, meters);
};

const toggleShowAverage = (state: SavedReportsState): SavedReportsState =>
  ({...state, meterPage: {...state.meterPage, shouldShowAverage: !state.meterPage.shouldShowAverage}});

const toggleLegendItemVisibility = (state: SavedReportsState, id: uuid): SavedReportsState => {
  const meters = getLegendItems(state).map(it => it.id === id ? {...it, isHidden: !it.isHidden} : it);
  return setLegendItems(state, meters);
};

const showHideAll = (state: SavedReportsState, legendType: LegendType): SavedReportsState => {
  const isAllLinesHidden = getViewOptions(state, legendType).isAllLinesHidden;
  const meters: LegendItem[] = getLegendItems(state)
    .map((prev: LegendItem) => prev.type === legendType ? {...prev, isHidden: !isAllLinesHidden} : prev);
  return toggleHiddenLines(setLegendItems(state, meters), legendType);
};

export const savedReports =
  (state: SavedReportsState = initialSavedReportState, action: ActionTypes): SavedReportsState => {
    switch (action.type) {
      case getType(addLegendItems):
        return setLegendItems(state, (action as Action<LegendItem[]>).payload);
      case getType(toggleLine):
        return toggleLegendItemVisibility(state, (action as Action<uuid>).payload);
      case getType(showHideAllByType):
        return showHideAll(state, getLegendType(action));
      case getType(removeAllByType):
        return removeAllByLegendType(state, getLegendType(action));
      case getType(showHideLegendRows):
        return setLegendItems(state, toggleLegendItemsRows(state, getLegendType(action)));
      case getType(toggleQuantityByType):
        return toggleQuantityLegendType(state, (action as Action<QuantityLegendType>).payload);
      case getType(toggleQuantityById):
        return toggleQuantityId(state, (action as Action<QuantityId>).payload);
      case getType(toggleShowAverageAction):
        return toggleShowAverage(state);
      case getType(logoutUser):
        return initialSavedReportState;
      default:
        return state;
    }
  };

export const report = combineReducers<ReportState>({
  savedReports,
  temporal
});
