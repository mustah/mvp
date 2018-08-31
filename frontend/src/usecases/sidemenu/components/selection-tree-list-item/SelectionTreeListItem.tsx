import IconButton from 'material-ui/IconButton';
import ActionZoomIn from 'material-ui/svg-icons/action/zoom-in';
import * as React from 'react';
import {listItemStyle, listItemStyleWithActions, nestedListItemStyle, sideBarStyles} from '../../../../app/themes';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {Normal} from '../../../../components/texts/Texts';
import {orUnknown} from '../../../../helpers/translations';
import {firstUpper, firstUpperTranslated} from '../../../../services/translationService';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import {SelectableListItem} from './SelectableListItem';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface RenderProps {
  id: uuid;
  openListItems: Set<uuid>;
  selectedListItems: Set<uuid>;
  selectionTree: SelectionTree;
  toggleExpand: OnClickWithId;
  toggleIncludingChildren: OnClick;
  toggleSingleEntry: OnClickWithId;
  itemCapabilities: ItemCapabilities;
  centerMapOnMeter: OnClickWithId;
}

export interface ItemCapabilities {
  zoomable?: boolean;
}

export const renderSelectionTreeCities = ({
  id, selectionTree, toggleSingleEntry, openListItems, ...other
}: RenderProps) => {
  const city = selectionTree.entities.cities[id];

  let nestedItems: Array<React.ReactElement<ListItemProps>> = [];
  if (city.clusters) {
    if (openListItems.has(id)) {
      nestedItems = [...city.clusters].sort()
        .map((id) => renderSelectionTreeClusters({...other, openListItems, toggleSingleEntry, selectionTree, id}));
    } else {
      // hack: let's not render stuff until we're expanded, but indicate that we are expandable by being non-empty
      nestedItems = [<React.Fragment key="city123"/>];
    }
  }

  return renderSelectableListItem({
    ...other,
    toggleSingleEntry,
    openListItems,
    id,
    selectable: true,
    primaryText: orUnknown(city.name),
    nestedItems,
  });
};

const renderSelectionTreeClusters = ({id, openListItems, selectionTree, ...other}: RenderProps) => {
  const cluster = selectionTree.entities.clusters[id];

  let nestedItems: Array<React.ReactElement<ListItemProps>> = [];
  if (cluster.addresses) {
    if (openListItems.has(id)) {
      nestedItems = [...cluster.addresses].sort().map((id) => renderSelectionTreeAddresses({
        ...other,
        selectionTree,
        openListItems,
        id,
      }));
    } else {
      // hack: let's not render stuff until we're expanded, but indicate that we are expandable by being non-empty
      nestedItems = [<React.Fragment key="cluster123"/>];
    }
  }

  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: cluster.name,
    openListItems,
    nestedItems,
  });
};

const renderSelectionTreeAddresses = ({id, openListItems, selectionTree, ...other}: RenderProps) => {
  const address = selectionTree.entities.addresses[id];

  // TODO is the id really an address..? don't think so
  let nestedItems: Array<React.ReactElement<ListItemProps>> = [];
  if (openListItems.has(id)) {
    nestedItems = [...address.meters]
      .sort()
      .map((id) => renderSelectionTreeMeters({...other, openListItems, selectionTree, id}));
  } else {
    // hack: let's not render stuff until we're expanded, but indicate that we are expandable by being non-empty
    nestedItems = [<React.Fragment key="address123"/>];
  }


  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: orUnknown(address.name),
    openListItems,
    nestedItems,
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
  itemCapabilities: ItemCapabilities;
  centerMapOnMeter: OnClickWithId;
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
  itemCapabilities,
  centerMapOnMeter,
}: Props) => {
  const onToggleExpand = nestedItems ? () => toggleExpand(id) : () => null;
  const onToggleSelect = nestedItems
    ? () => toggleIncludingChildren(id)
    : () => toggleSingleEntry(id);

  const zoomInOn = (ev: React.SyntheticEvent<{}>) => {
    ev.stopPropagation();
    centerMapOnMeter(id);
  };

  const content = !nestedItems && itemCapabilities.zoomable
    ? (
      <RowCenter className="space-between">
        <Row className="first-uppercase" style={listItemStyleWithActions.textStyle}>
          <Normal title={firstUpper(primaryText)}>
            {primaryText}
          </Normal>
        </Row>
        <Row style={{marginRight: '24px'}}>
          <IconButton
            tooltip={firstUpperTranslated('show on map')}
            onClick={zoomInOn}
          >
            <ActionZoomIn/>
          </IconButton>
        </Row>
      </RowCenter>
    )
    : (
      <Normal
        className="first-uppercase"
        style={listItemStyle.textStyle}
        title={firstUpper(primaryText)}
      >
        {primaryText}
      </Normal>
    );

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={content}
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
