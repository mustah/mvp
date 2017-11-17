import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {uuid} from '../../../../types/Types';
import {listItemStyle, listStyle, nestedListItemStyle, sideBarHeaderStyle, sideBarStyles} from '../../../app/themes';
import {SelectionTreeModel} from '../../../../state/domain-models/meter/meterModels';
import './SelectionTreeContainer.scss';
import ListItemProps = __MaterialUI.List.ListItemProps;
import {getSelectionTree} from '../../../../state/domain-models/meter/meterSelectors';
import {selectionTreeToggleEntry} from '../../../../state/ui/selection-tree/selectionTreeActions';

interface SelectionTreeProps {
  topLevel: string;
}

interface StateToProps {
  selectionTree: any;
}

interface DispatchToProps {
  selectionTreeToggleEntry: (id: uuid) => void;
}

const SelectionTree = (props: SelectionTreeProps & StateToProps & DispatchToProps) => {
  if (props.selectionTree.result.length < 1) {
    return null;
  }
  const {topLevel, selectionTree} = props;
  const renderSelectionOverview = (id: uuid) => renderSelectionTree(id, selectionTree, topLevel);
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

const mapStateToProps = ({domainModels: {meters}}: RootState): StateToProps => {
  return {
    selectionTree: getSelectionTree(meters),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectionTreeToggleEntry,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps, SelectionTreeProps>(mapStateToProps, mapDispatchToProps)(SelectionTree);

const renderSelectionTree = (id: uuid, data: SelectionTreeModel, level: string) => {
  const entity = data.entities[level][id];
  const nextLevel = entity.childNodes.type;
  const selectable = entity.selectable;

  const renderChildNodes = (treeItem: uuid) => renderSelectionTree(treeItem, data, nextLevel);
  const nestedItems = entity.childNodes.ids.sort().map(renderChildNodes);

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={entity.name}
      key={id}
      innerDivStyle={sideBarStyles.padding}
      initiallyOpen={false}
      nestedItems={nestedItems}
      nestedListStyle={nestedListItemStyle}
      selectable={selectable}
    />
  );
};

class SelectableListItem extends React.Component<ListItemProps & {selectable: boolean}, {selected: boolean}> {

  state = {selected: false};

  render() {
    const {selectable, ...ListItemProps} = this.props;
    const selectableStyle: React.CSSProperties = selectable ? {} : sideBarStyles.notSelectable;
    const selectedStyle: React.CSSProperties = this.state.selected ? sideBarStyles.selected : selectableStyle;
    return (
      <ListItem
        {...ListItemProps}
        style={{...listItemStyle, ...selectedStyle}}
        hoverColor={sideBarStyles.onHover.color}
        onClick={selectable ? this.onClick : () => null}
      />
    );
  }

  onClick = (): void => {
    this.setState((prevState => ({selected: !prevState.selected})));
  }

}
