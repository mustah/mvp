import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Period, TemporalResolution} from '../../components/dates/dateModels';
import {toggle} from '../../helpers/collections';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Action, uuid} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import {
  addLegendItems,
  removeAllByMedium,
  selectResolution,
  setReportTimePeriod,
  showHideAllByMedium,
  showHideMediumRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByMedium,
} from './reportActions';
import {
  LegendItem,
  MediumViewOptions,
  QuantityId,
  QuantityMedium,
  Report,
  ReportState,
  ViewOptions
} from './reportModels';
import {getLegendItems, getMediumViewOptions, getViewOptions} from './reportSelectors';

export const mediumViewOptions: MediumViewOptions = {
  [Medium.districtHeating]: {quantities: []},
  [Medium.gas]: {quantities: []},
  [Medium.water]: {quantities: []},
  [Medium.hotWater]: {quantities: []},
  [Medium.electricity]: {quantities: []},
  [Medium.roomSensor]: {quantities: []},
  [Medium.unknown]: {quantities: []},
};

export const initialState: ReportState = {
  resolution: TemporalResolution.hour,
  savedReports: {
    meterPage: {
      id: 'meterPage',
      meters: [],
      mediumViewOptions,
    }
  },
  timePeriod: {
    period: Period.latest,
  },
};

const getMedium = (action: ActionTypes): Medium => (action as Action<Medium>).payload;

const toggleLegendItemsRows = (state: ReportState, medium: Medium): LegendItem[] =>
  getLegendItems(state).map(it => it.medium === medium ? {...it, isRowExpanded: !it.isRowExpanded} : it);

const getLegendItemsNotMatchingMedium = (state: ReportState, medium: Medium): LegendItem[] =>
  getLegendItems(state).filter(it => it.medium !== medium);

const getLegendItemsMatchingMedium = (state: ReportState, medium: Medium): LegendItem[] =>
  getLegendItems(state).filter(it => it.medium === medium);

const savedReports = (state: ReportState, meters: LegendItem[]): ObjectsById<Report> => ({
    ...state.savedReports,
    ['meterPage']: {
      ...state.savedReports.meterPage,
      meters
    }
  }
);

const toggleHiddenLines = (savedReports: ObjectsById<Report>, medium: Medium): ObjectsById<Report> => {
  if (savedReports.meterPage.meters.filter(it => it.medium === medium).length > 0) {
    const mediumViewOptions: MediumViewOptions = savedReports.meterPage.mediumViewOptions;
    const viewOptions: ViewOptions = mediumViewOptions[medium];
    return ({
        ...savedReports,
        ['meterPage']: {
          ...savedReports.meterPage,
          mediumViewOptions: {
            ...mediumViewOptions,
            [medium]: {...viewOptions, isAllLinesHidden: !viewOptions.isAllLinesHidden}
          }
        }
      }
    );
  }
  return savedReports;
};

const toggleQuantityMedium = (state: ReportState, {medium, quantity}: QuantityMedium): ObjectsById<Report> => {
  const mediumViewOptions = getMediumViewOptions(state);
  const viewOptions: ViewOptions = mediumViewOptions[medium];
  const quantities = toggle(quantity, viewOptions.quantities);
  const meters = getLegendItems(state).map(it => it.medium === medium ? {...it, quantities} : it);
  return {
    ...state.savedReports,
    ['meterPage']: {
      ...state.savedReports.meterPage,
      meters,
      mediumViewOptions: {
        ...mediumViewOptions,
        [medium]: {...viewOptions, quantities}
      }
    }
  };
};

const toggleQuantityId = (state: ReportState, {id, quantity}: QuantityId): ObjectsById<Report> => {
  const meters = getLegendItems(state).map(
    it => it.id === id ? {...it, quantities: toggle(quantity, it.quantities)} : it);
  return savedReports(state, meters);
};

const toggleLegendItemVisibility = (state: ReportState, id: uuid): ReportState => ({
  ...state,
  savedReports: savedReports(
    state,
    getLegendItems(state).map(it => it.id === id ? {...it, isHidden: !it.isHidden} : it)
  )
});

const showHideAll = (state: ReportState, medium: Medium): ReportState => {
  const isAllLinesHidden = getViewOptions(state, medium).isAllLinesHidden;
  const meters: LegendItem[] = getLegendItemsMatchingMedium(state, medium)
    .map(it => ({...it, isHidden: !isAllLinesHidden}));
  return {
    ...state,
    savedReports: toggleHiddenLines(savedReports(state, meters), medium),
  };
};

type ActionTypes =
  | Action<LegendItem[] | string[] | uuid | TemporalResolution | SelectionInterval | Medium>
  | Action<QuantityMedium | QuantityId>
  | EmptyAction<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case getType(addLegendItems):
      return {
        ...state,
        savedReports: savedReports(state, (action as Action<LegendItem[]>).payload)
      };
    case getType(setReportTimePeriod):
      return {
        ...state,
        timePeriod: {...(action as Action<SelectionInterval>).payload}
      };
    case getType(selectResolution):
      return {
        ...state,
        resolution: (action as Action<TemporalResolution>).payload,
      };
    case getType(toggleLine):
      return toggleLegendItemVisibility(state, (action as Action<uuid>).payload);
    case getType(showHideAllByMedium):
      return showHideAll(state, getMedium(action));
    case getType(removeAllByMedium):
      return {
        ...state,
        savedReports: savedReports(state, getLegendItemsNotMatchingMedium(state, getMedium(action))),
      };
    case getType(showHideMediumRows):
      return {
        ...state,
        savedReports: savedReports(state, toggleLegendItemsRows(state, getMedium(action))),
      };
    case getType(toggleQuantityByMedium):
      return {
        ...state,
        savedReports: toggleQuantityMedium(state, (action as Action<QuantityMedium>).payload),
      };
    case getType(toggleQuantityById):
      return {
        ...state,
        savedReports: toggleQuantityId(state, (action as Action<QuantityId>).payload),
      };
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
