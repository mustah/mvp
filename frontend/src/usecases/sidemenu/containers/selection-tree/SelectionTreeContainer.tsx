import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {listStyle, nestedListItemStyle, sideBarHeaderStyle, sideBarStyles} from '../../../../app/themes';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {SelectionTreeData} from '../../../../state/domain-models-paginated/meter/meterModels';
import {getSelectionTree} from '../../../../state/domain-models-paginated/meter/meterSelectors';
import {selectionTreeToggleId} from '../../../../state/ui/selection-tree/selectionTreeActions';
import {getOpenListItems} from '../../../../state/ui/selection-tree/selectionTreeSelectors';
import {OnClickWithId, uuid} from '../../../../types/Types';
import {selectEntryToggle} from '../../../report/reportActions';
import {getSelectedListItems} from '../../../report/reportSelectors';
import {renderSelectionTree} from '../../components/selection-tree-list-item/SelectionTreeListItem';
import './SelectionTreeContainer.scss';

interface SelectionTreeProps {
  topLevel: string;
}

interface StateToProps {
  selectionTree: SelectionTreeData;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
}

interface DispatchToProps {
  toggleExpand: OnClickWithId;
  toggleSelect: OnClickWithId;
}

const SelectionTree = (props: SelectionTreeProps & StateToProps & DispatchToProps) => {
  if (Object.keys(props.selectionTree.result).length === 0) {
    return null;
  }
  const {topLevel, selectionTree, toggleExpand, openListItems, toggleSelect, selectedListItems} = props;
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
        initiallyOpen={false}
        style={sideBarHeaderStyle}
        hoverColor={sideBarStyles.onHover.color}
        nestedItems={nestedItems}
        nestedListStyle={nestedListItemStyle}
      />
    </List>
  );
};

const mapStateToProps = ({report, domainModels: {metersAll}, ui: {selectionTree}}: RootState): StateToProps => {
  return {
    selectionTree: getSelectionTree(metersAll),
    openListItems: getOpenListItems(selectionTree),
    selectedListItems: getSelectedListItems(report),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleExpand: selectionTreeToggleId,
  toggleSelect: selectEntryToggle,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps, SelectionTreeProps>(mapStateToProps, mapDispatchToProps)(SelectionTree);
