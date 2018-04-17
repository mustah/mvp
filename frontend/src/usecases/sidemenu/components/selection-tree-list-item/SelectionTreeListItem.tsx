import * as React from 'react';
import {listItemStyle, nestedListItemStyle, sideBarStyles} from '../../../../app/themes';
import {locationNameTranslation} from '../../../../helpers/translations';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {OnClickWithId, uuid} from '../../../../types/Types';
import {SelectableListItem} from './SelectableListItem';

interface RenderProps {
  id: uuid;
  selectionTree: SelectionTree;
  toggleExpand: OnClickWithId;
  toggleSelect: OnClickWithId;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
}

export const renderSelectionTreeCities = ({id, selectionTree, ...other}: RenderProps) => {
  const city = selectionTree.entities.cities[id];
  const clusters = city.clusters.sort()
    .map((id) => renderSelectionTreeClusters({...other, selectionTree, id}));
  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: locationNameTranslation(city.name),
    nestedItems: clusters,
  });
};

const renderSelectionTreeClusters = ({id, selectionTree, ...other}: RenderProps) => {
  const cluster = selectionTree.entities.clusters[id];
  const addresses = cluster.addresses.sort().map((id) => renderSelectionTreeAddresses({...other, selectionTree, id}));
  return renderSelectableListItem({
    ...other,
    id,
    selectable: false,
    primaryText: cluster.name,
    nestedItems: addresses,
  });
};

const renderSelectionTreeAddresses = ({id, selectionTree, ...other}: RenderProps) => {
  const address = selectionTree.entities.addresses[id];
  const meters = address.meters.sort().map((id) => renderSelectionTreeMeters({...other, selectionTree, id}));
  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: locationNameTranslation(address.name),
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
  primaryText: string;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
  toggleExpand: OnClickWithId;
  toggleSelect: OnClickWithId;
  selectable: boolean;
  nestedItems?: Array<React.ReactElement<any>>;
}

const renderSelectableListItem = ({
  id,
  primaryText,
  openListItems,
  toggleExpand,
  toggleSelect,
  selectedListItems,
  selectable,
  nestedItems,
}: Props) => {
  const onToggleExpand = nestedItems ? () => toggleExpand(id) : () => null;
  const onToggleSelect = selectable ? () => toggleSelect(id) : () => null;

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
    />);
};
