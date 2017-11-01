import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import 'SelectionTree.scss';
import {translate} from '../../../../services/translationService';
import {SelectionTreeModel, selectionTreeData} from '../../models/organizedData';
import ListItemProps = __MaterialUI.List.ListItemProps;
import {selectionTreeItems, sideBarHeaders} from '../../../app/themes';
import {uuid} from '../../../../types/Types';

interface SelectionTreeProps {
  topLevel: string;
}

export const SelectionTree = (props: SelectionTreeProps) => {
  const {topLevel} = props;
  const topLevelList = selectionTreeData.result[topLevel];
  const renderSelectionOverview = (id) =>
    renderSelectionTree(id, selectionTreeData, topLevel);

  return (
    <List>
      <ListItem
        className="ListItem"
        primaryText={translate('selection overview')}
        initiallyOpen={false}
        style={sideBarHeaders.fontStyle}
        nestedItems={topLevelList.map(renderSelectionOverview)}
      />
    </List>
  );
};

const renderSelectionTree = (id: uuid, data: SelectionTreeModel, level: string) => {
  const entity = data.entities[level][id];
  const nextLevel = entity.childNodes.type;

  const renderChildNodes = (treeItem) => renderSelectionTree(treeItem, data, nextLevel);

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={entity.name}
      key={id}
      style={selectionTreeItems.fontSize}
      innerDivStyle={selectionTreeItems.padding}
      initiallyOpen={false}
      nestedItems={entity.childNodes.ids.map(renderChildNodes)}
    />
  );
};

class SelectableListItem extends React.Component<ListItemProps, {selected: boolean}> {

  state = {selected: false};

  onClick = (): void => {
    this.setState((prevState => ({selected: !prevState.selected})));
  }

  render() {
    const selected = this.state.selected ? selectionTreeItems.selected : null;
    return (
      <ListItem
        {...this.props}
        style={{...this.props.style, ...selected}}
        onClick={this.onClick}
      />
    );
  }
}
