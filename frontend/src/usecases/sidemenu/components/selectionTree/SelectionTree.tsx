import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import 'SelectionTree.scss';
import {translate} from '../../../../services/translationService';
import {uuid} from '../../../../types/Types';
import {listItemStyle, listStyle, nestedListItemStyle, sideBarHeaderStyle, sideBarStyles} from '../../../app/themes';
import {SelectionTreeModel} from '../../models/organizedData';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface SelectionTreeProps {
  topLevel: string;
  sidebarTree: any;
}

export const SelectionTree = (props: SelectionTreeProps) => {

  if (props.sidebarTree.result.length < 1) {
    return null;
  }
  const {topLevel, sidebarTree} = props;
  const renderSelectionOverview = (id: uuid) => renderSelectionTree(id, sidebarTree, topLevel);
  const nestedItems = sidebarTree.result[topLevel].sort().map(renderSelectionOverview);

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
    const selectedStyle: React.CSSProperties = this.state.selected ? sideBarStyles.selected : {};
    const {selectable, ...ListItemProps} = this.props;
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
