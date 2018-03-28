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
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {fetchAllMeters} from '../../../../state/domain-models/meter-all/allMetersApiActions';
import {SelectionTreeData} from '../../../../state/domain-models/meter-all/allMetersModels';
import {getSelectionTree} from '../../../../state/domain-models/meter-all/allMetersSelectors';
import {getEncodedUriParametersForAllMeters} from '../../../../state/search/selection/selectionSelectors';
import {selectionTreeToggleId} from '../../../../state/ui/selection-tree/selectionTreeActions';
import {getOpenListItems} from '../../../../state/ui/selection-tree/selectionTreeSelectors';
import {OnClickWithId, Fetch, uuid} from '../../../../types/Types';
import {selectEntryToggle} from '../../../report/reportActions';
import {getSelectedListItems} from '../../../report/reportSelectors';
import {renderSelectionTree} from '../../components/selection-tree-list-item/SelectionTreeListItem';
import './SelectionTreeContainer.scss';

interface OwnProps {
  topLevel: string;
}

interface StateToProps {
  selectionTree: SelectionTreeData;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
  encodedUriParametersForAllMeters: string;
}

interface DispatchToProps {
  toggleExpand: OnClickWithId;
  toggleSelect: OnClickWithId;
  fetchAllMeters: Fetch;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class SelectionTree extends React.Component<Props> {

  componentDidMount() {
    const {fetchAllMeters, encodedUriParametersForAllMeters} = this.props;
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  componentWillReceiveProps({fetchAllMeters, encodedUriParametersForAllMeters}: Props) {
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  render() {
    if (Object.keys(this.props.selectionTree.result).length === 0) {
      return null;
    }
    const {topLevel, selectionTree, toggleExpand, openListItems, toggleSelect, selectedListItems} = this.props;
    const renderSelectionOverview = (id: uuid) => renderSelectionTree({
      id,
      data: selectionTree,
      level: topLevel,
      toggleExpand,
      openListItems,
      toggleSelect,
      selectedListItems,
    });

    const nestedItems = selectionTree.result[topLevel].sort().map(renderSelectionOverview);
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

const mapStateToProps =
  ({report, searchParameters, domainModels: {allMeters}, ui: {selectionTree}}: RootState): StateToProps => {
    return {
      selectionTree: getSelectionTree(allMeters),
      openListItems: getOpenListItems(selectionTree),
      selectedListItems: getSelectedListItems(report),
      encodedUriParametersForAllMeters: getEncodedUriParametersForAllMeters(searchParameters),
    };
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleExpand: selectionTreeToggleId,
  toggleSelect: selectEntryToggle,
  fetchAllMeters,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(SelectionTree);
