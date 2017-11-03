import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import 'SelectionTree.scss';
import {translate} from '../../../../services/translationService';
import {uuid} from '../../../../types/Types';
import {listItemStyle, sideBarStyles, sideBarHeaderStyle, listStyle, nestedListItemStyle} from '../../../app/themes';
import {selectionTreeData, SelectionTreeModel} from '../../models/organizedData';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface SelectionTreeProps {
  topLevel: string;
}

export const SelectionTree = (props: SelectionTreeProps) => {
  const {topLevel} = props;
  const renderSelectionOverview = (id: uuid) => renderSelectionTree(id, selectionTreeData, topLevel);
  const nestedItems = selectionTreeData.result[topLevel].map(renderSelectionOverview);

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

  const renderChildNodes = (treeItem: uuid) => renderSelectionTree(treeItem, data, nextLevel);
  const nestedItems = entity.childNodes.ids.map(renderChildNodes);

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={entity.name}
      key={id}
      innerDivStyle={sideBarStyles.padding}
      initiallyOpen={false}
      nestedItems={nestedItems}
      nestedListStyle={nestedListItemStyle}
    />
  );
};

class SelectableListItem extends React.Component<ListItemProps, {selected: boolean}> {

  state = {selected: false};

  render() {
    const selected = this.state.selected ? sideBarStyles.selected : null;
    return (
      <ListItem
        {...this.props}
        style={{...listItemStyle, ...selected}}
        hoverColor={sideBarStyles.onHover.color}
        onClick={this.onClick}
      />
    );
  }

  onClick = (): void => {
    this.setState((prevState => ({selected: !prevState.selected})));
  }

}
