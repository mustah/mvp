import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Period, TemporalResolution} from '../../components/dates/dateModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Action, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {
  hideAllByMedium,
  removeAllByMedium,
  SELECT_RESOLUTION,
  setReportTimePeriod,
  setSelectedItems,
  showHideMediumRows,
  toggleLine,
} from './reportActions';
import {LegendItem, MediumViewOptions, Report, ReportState, SelectedReportPayload, ViewOption} from './reportModels';
import {getLegendItems, getMediumViewOptions} from './reportSelectors';

export const mediumViewOptions: MediumViewOptions = {
  [Medium.districtHeating]: {},
  [Medium.gas]: {},
  [Medium.water]: {},
  [Medium.hotWater]: {},
  [Medium.electricity]: {},
  [Medium.roomSensor]: {},
  [Medium.unknown]: {},
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

const getMediumViewOption = (state: ReportState, medium: Medium): ViewOption => getMediumViewOptions(state)[medium];

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
    const option = mediumViewOptions[medium];
    return ({
        ...savedReports,
        ['meterPage']: {
          ...savedReports.meterPage,
          mediumViewOptions: {
            ...mediumViewOptions,
            [medium]: {...option, isAllLinesHidden: !option.isAllLinesHidden}
          }
        }
      }
    );
  }
  return savedReports;
};

const toggleLegendItemVisibility = (state: ReportState, id: uuid): ReportState => ({
  ...state,
  savedReports: savedReports(
    state,
    getLegendItems(state).map(it => it.id === id ? {...it, isHidden: !it.isHidden} : it)
  )
});

const toggleShowHideAllByMedium = (state: ReportState, action: ActionTypes): ReportState => {
  const medium: Medium = getMedium(action);
  const isAllLinesHidden = getMediumViewOption(state, medium).isAllLinesHidden;
  const meters: LegendItem[] = getLegendItemsMatchingMedium(state, medium)
    .map(it => ({...it, isHidden: !isAllLinesHidden}));
  return {
    ...state,
    savedReports: toggleHiddenLines(savedReports(state, meters), medium),
  };
};

type ActionTypes =
  | Action<SelectedReportPayload | string[] | uuid | TemporalResolution | SelectionInterval | Medium>
  | EmptyAction<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case getType(setSelectedItems):
      return {
        ...state,
        savedReports: savedReports(state, (action as Action<SelectedReportPayload>).payload.items)
      };
    case getType(setReportTimePeriod):
      return {
        ...state,
        timePeriod: {...(action as Action<SelectionInterval>).payload}
      };
    case SELECT_RESOLUTION:
      return {
        ...state,
        resolution: (action as Action<TemporalResolution>).payload,
      };
    case getType(toggleLine):
      return toggleLegendItemVisibility(state, (action as Action<uuid>).payload);
    case getType(hideAllByMedium):
      return toggleShowHideAllByMedium(state, action);
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
    case LOGOUT_USER:
      return initialState;
    default:
      return state;
  }
};
