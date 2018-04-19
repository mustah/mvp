import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {
  listStyle,
  nestedListItemStyle,
  sideBarHeaderStyle,
  sideBarStyles,
} from '../../../../app/themes';
import {now} from '../../../../helpers/dateHelpers';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {getMeterParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {fetchSelectionTree} from '../../../../state/selection-tree/selectionTreeApiActions';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {getSelectionTree} from '../../../../state/selection-tree/selectionTreeSelectors';
import {selectionTreeToggleId} from '../../../../state/ui/selection-tree/selectionTreeActions';
import {getOpenListItems} from '../../../../state/ui/selection-tree/selectionTreeSelectors';
import {EncodedUriParameters, Fetch, OnClickWithId, uuid} from '../../../../types/Types';
import {selectEntryToggle} from '../../../report/reportActions';
import {getSelectedListItems} from '../../../report/reportSelectors';
import './SelectionTreeContainer.scss';
import {renderSelectionTreeCities} from '../../components/selection-tree-list-item/SelectionTreeListItem';

interface StateToProps {
  selectionTree: SelectionTree;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
  parameters: EncodedUriParameters;
}

interface DispatchToProps {
  toggleExpand: OnClickWithId;
  toggleSelect: OnClickWithId;
  fetchSelectionTree: Fetch;
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
    const {selectionTree, toggleExpand, openListItems, toggleSelect, selectedListItems} = this.props;
    if (selectionTree.result.cities.length === 0) {
      return null;
    }
    const renderSelectionOverview = (id: uuid) => renderSelectionTreeCities({
      id,
      selectionTree,
      toggleExpand,
      openListItems,
      toggleSelect,
      selectedListItems,
    });

    const nestedItems = selectionTree.result.cities.sort().map(renderSelectionOverview);
    return (
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
    );
  }
}

const mapStateToProps = (
  {
    report,
    userSelection: {userSelection},
    selectionTree,
    ui: {selectionTree: selectionTreeUi},
  }: RootState): StateToProps => {
  return {
    selectionTree: getSelectionTree(selectionTree),
    openListItems: getOpenListItems(selectionTreeUi),
    selectedListItems: getSelectedListItems(report),
    parameters: getMeterParameters({
      userSelection,
      now: now(),
    }),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleExpand: selectionTreeToggleId,
  toggleSelect: selectEntryToggle,
  fetchSelectionTree,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(SelectionTreeComponent);
