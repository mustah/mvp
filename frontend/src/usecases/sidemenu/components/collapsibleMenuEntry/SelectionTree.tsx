import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import 'SelectionTree.scss';
import {translate} from '../../../../services/translationService';
import {selectionTreeData} from '../../models/organizedData';
import ListItemProps = __MaterialUI.List.ListItemProps;
import {selectionTreeItems, sideBarHeaders} from '../../../app/themes';

interface SelectionTreeProps {
  topLvl: string;
}

export const SelectionTree = (props: SelectionTreeProps) => {
  const {topLvl} = props;
  const topLvlList = selectionTreeData.result[topLvl];
  const renderSelectionOverview = (id) =>
    renderSelectionTree(id, selectionTreeData, topLvl);

  return (
    <List>
      <ListItem
        className="ListItem"
        primaryText={translate('selection overview')}
        initiallyOpen={false}
        style={sideBarHeaders.fontStyle}
        nestedItems={topLvlList.map(renderSelectionOverview)}
      />
    </List>
  );
};

const renderSelectionTree = (id, data, level) => {
  const entity = data.entities[level][id];
  const nextLevel = entity.childNodes.type;

  const mapFnc = (treeItem) => renderSelectionTree(treeItem, data, nextLevel);

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={entity.name}
      key={id}
      style={selectionTreeItems.fontSize}
      innerDivStyle={selectionTreeItems.padding}
      initiallyOpen={false}
      nestedItems={entity.childNodes.ids.map(mapFnc)}
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
