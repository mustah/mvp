import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {listStyle, nestedListItemStyle, sideBarHeaderStyle, sideBarStyles} from '../../../../app/themes';
import {SearchBox} from '../../../../components/search-box/SearchBox';
import {now} from '../../../../helpers/dateHelpers';
import {RootState} from '../../../../reducers/rootReducer';
import {isDashboardPage, isReportPage} from '../../../../selectors/routerSelectors';
import {translate} from '../../../../services/translationService';
import {fetchSelectionTree} from '../../../../state/selection-tree/selectionTreeApiActions';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {getSelectionTree} from '../../../../state/selection-tree/selectionTreeSelectors';
import {selectionTreeToggleId} from '../../../../state/ui/selection-tree/selectionTreeActions';
import {getOpenListItems} from '../../../../state/ui/selection-tree/selectionTreeSelectors';
import {getMeterParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch, OnChange, OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import {centerMapOnMeter} from '../../../dashboard/dashboardActions';
import {toggleIncludingChildren, toggleSingleEntry, addToReport} from '../../../report/reportActions';
import {getSelectedListItems} from '../../../report/reportSelectors';
import {clearSelectionTreeSearch, selectionTreeSearch} from '../../../search/searchActions';
import {OnSearch, Query} from '../../../search/searchModels';
import {LoadingListItem} from '../../components/LoadingListItem';
import {
  ItemCapabilities,
  renderSelectionTreeCities,
} from '../../components/selection-tree-list-item/SelectionTreeListItem';
import './SelectionTreeContainer.scss';

interface StateToProps extends Query {
  isFetching: boolean;
  selectionTree: SelectionTree;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
  parameters: EncodedUriParameters;
  itemCapabilities: ItemCapabilities;
}

interface DispatchToProps {
  addToReport: OnClickWithId;
  fetchSelectionTree: Fetch;
  toggleExpand: OnClickWithId;
  toggleSingleEntry: OnClickWithId;
  toggleIncludingChildren: OnClick;
  centerMapOnMeter: OnClickWithId;
  selectionTreeSearch: OnSearch;
  clearSearch: OnChange;
}

type Props = StateToProps & DispatchToProps;

class SelectionTreeComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchSelectionTree, parameters} = this.props;
    fetchSelectionTree(parameters);
  }

  componentWillReceiveProps({fetchSelectionTree, parameters}: Props) {
    fetchSelectionTree(parameters);
  }

  render() {
    const {
      addToReport,
      clearSearch,
      isFetching,
      selectionTree,
      toggleExpand,
      openListItems,
      selectedListItems,
      toggleIncludingChildren,
      toggleSingleEntry,
      itemCapabilities,
      centerMapOnMeter,
      selectionTreeSearch,
      query,
    } = this.props;

    const renderSelectionOverview = (id: uuid) =>
      renderSelectionTreeCities({
        addToReport,
        id,
        selectionTree,
        toggleExpand,
        openListItems,
        selectedListItems,
        toggleIncludingChildren,
        toggleSingleEntry,
        itemCapabilities,
        centerMapOnMeter,
      });

    const cityIds = selectionTree.result.cities;
    const nestedItems = cityIds.length
      ? [...cityIds].sort().map(renderSelectionOverview)
      : [(
           <LoadingListItem
             isFetching={isFetching}
             text={translate('no meters')}
             key="loading-list-item"
           />
         )];

    return (
      <React.Fragment>
        <SearchBox
          onChange={selectionTreeSearch}
          onClear={clearSearch}
          value={query}
          className="SearchBox-list SearchBox-tree"
        />
        <List style={listStyle}>
          <ListItem
            className="ListItem"
            primaryText={translate('selection overview')}
            initiallyOpen={true}
            style={sideBarHeaderStyle}
            hoverColor={sideBarStyles.onHover.color}
            nestedItems={nestedItems}
            nestedListStyle={nestedListItemStyle}
          />
        </List>
      </React.Fragment>
    );
  }
}

const mapStateToProps =
  ({
    report,
    userSelection: {userSelection},
    selectionTree,
    ui: {selectionTree: selectionTreeUi},
    routing,
    search: {selectionTree: {query}},
  }: RootState): StateToProps =>
    ({
      isFetching: selectionTree.isFetching,
      selectionTree: getSelectionTree({...selectionTree, query}),
      openListItems: getOpenListItems(selectionTreeUi),
      selectedListItems: getSelectedListItems(report),
      parameters: getMeterParameters({userSelection, now: now()}),
      itemCapabilities: {
        zoomable: isDashboardPage(routing),
        report: isReportPage(routing),
      },
      query,
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  centerMapOnMeter,
  clearSearch: clearSelectionTreeSearch,
  addToReport,
  fetchSelectionTree,
  selectionTreeSearch,
  toggleExpand: selectionTreeToggleId,
  toggleSingleEntry,
  toggleIncludingChildren,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(SelectionTreeComponent);
