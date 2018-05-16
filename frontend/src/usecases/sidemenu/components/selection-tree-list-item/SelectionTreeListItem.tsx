import * as React from 'react';
import {listItemStyle, nestedListItemStyle, sideBarStyles} from '../../../../app/themes';
import {orUnknown} from '../../../../helpers/translations';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import {SelectableListItem} from './SelectableListItem';

interface RenderProps {
  id: uuid;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
  selectionTree: SelectionTree;
  toggleExpand: OnClickWithId;
  toggleIncludingChildren: OnClick;
  toggleSingleEntry: OnClickWithId;
}

export const renderSelectionTreeCities = ({id, selectionTree, toggleSingleEntry, ...other}: RenderProps) => {
  const city = selectionTree.entities.cities[id];
  const clusters = [...city.clusters].sort()
    .map((id) => renderSelectionTreeClusters({...other, toggleSingleEntry, selectionTree, id}));
  return renderSelectableListItem({
    ...other,
    toggleSingleEntry,
    id,
    selectable: true,
    primaryText: orUnknown(city.name),
    nestedItems: clusters,
  });
};

const renderSelectionTreeClusters = ({id, selectionTree, ...other}: RenderProps) => {
  const cluster = selectionTree.entities.clusters[id];
  const addresses = [...cluster.addresses].sort().map((id) => renderSelectionTreeAddresses({
    ...other,
    selectionTree,
    id,
  }));
  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: cluster.name,
    nestedItems: addresses,
  });
};

const renderSelectionTreeAddresses = ({id, selectionTree, ...other}: RenderProps) => {
  const address = selectionTree.entities.addresses[id];
  const meters = [...address.meters].sort().map((id) => renderSelectionTreeMeters({...other, selectionTree, id}));
  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: orUnknown(address.name),
    nestedItems: meters,
  });
};

const renderSelectionTreeMeters = ({id, selectionTree, ...other}: RenderProps) => {
  const meter = selectionTree.entities.meters[id];
  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: meter.name,
  });
};

interface Props {
  id: uuid;
  nestedItems?: Array<React.ReactElement<any>>;
  openListItems: Set<uuid>;
  primaryText: string;
  selectable: boolean;
  selectedListItems: Set<uuid>;
  toggleExpand: OnClickWithId;
  toggleIncludingChildren: OnClick;
  toggleSingleEntry: OnClickWithId;
}

const renderSelectableListItem = ({
  id,
  primaryText,
  openListItems,
  toggleExpand,
  toggleSingleEntry,
  toggleIncludingChildren,
  selectedListItems,
  selectable,
  nestedItems,
}: Props) => {
  const onToggleExpand = nestedItems ? () => toggleExpand(id) : () => null;
  const onToggleSelect = nestedItems
    ? () => toggleIncludingChildren(id)
    : () => toggleSingleEntry(id);

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={<div style={listItemStyle.textStyle}>{primaryText}</div>}
      key={id}
      innerDivStyle={sideBarStyles.padding}
      initiallyOpen={openListItems.has(id)}
      nestedListStyle={nestedListItemStyle}
      nestedItems={nestedItems}
      onNestedListToggle={onToggleExpand}
      onClick={onToggleSelect}
      selectable={selectable}
      selected={selectedListItems.has(id)}
    />
  );
};
