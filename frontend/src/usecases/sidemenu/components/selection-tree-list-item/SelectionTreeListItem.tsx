import * as React from 'react';
import {listItemStyle, listItemStyleWithActions, nestedListItemStyle, sideBarStyles} from '../../../../app/themes';
import {MediumButton} from '../../../../components/buttons/MediumButton';
import {ZoomButton} from '../../../../components/buttons/ZoomButton';
import {OpenDialogInfoButton} from '../../../../components/dialog/OpenDialogInfoButton';
import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import '../../../../components/indicators/ReportIndicatorWidget.scss';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {Normal} from '../../../../components/texts/Texts';
import {MeterDetailsContainer} from '../../../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../../../helpers/Maybe';
import {orUnknown} from '../../../../helpers/translations';
import {firstUpper} from '../../../../services/translationService';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import {SelectableListItem} from './SelectableListItem';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface RenderProps {
  addToReport: OnClickWithId;
  id: uuid;
  openListItems: Set<uuid>;
  selectionTree: SelectionTree;
  toggleExpand: OnClickWithId;
  toggleIncludingChildren: OnClick;
  toggleSingleEntry: OnClickWithId;
  itemOptions: ItemOptions;
  centerMapOnMeter: OnClickWithId;
}

export interface ItemOptions {
  hasInfoButton?: boolean;
  zoomable?: boolean;
  report?: boolean;
}

export const renderSelectionTreeCities = ({
  id, selectionTree, toggleSingleEntry, openListItems, ...other,
}: RenderProps) => {
  const city = selectionTree.entities.cities[id];

  let nestedItems: Array<React.ReactElement<ListItemProps>> = [];
  if (city.clusters) {
    if (openListItems.has(id)) {
      nestedItems = [...city.clusters].sort()
        .map((id) => renderSelectionTreeClusters({
          ...other,
          openListItems,
          toggleSingleEntry,
          selectionTree,
          id,
        }));
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
  const {name, medium} = selectionTree.entities.meters[id];
  return renderSelectableListItem({
    ...other,
    id,
    selectable: false,
    primaryText: name,
    medium,
  });
};

const iconRowStyle: React.CSSProperties = {marginRight: '18px'};

interface Props {
  addToReport: OnClickWithId;
  id: uuid;
  nestedItems?: Array<React.ReactElement<any>>;
  openListItems: Set<uuid>;
  primaryText: string;
  selectable: boolean;
  toggleExpand: OnClickWithId;
  toggleIncludingChildren: OnClick;
  toggleSingleEntry: OnClickWithId;
  itemOptions: ItemOptions;
  centerMapOnMeter: OnClickWithId;
  medium?: Medium;
}

const labelStyle: React.CSSProperties = {
  marginLeft: 4,
  paddingLeft: 0,
  paddingRight: 0,
  textOverflow: 'ellipsis',
};

const renderSelectableListItem = ({
  addToReport,
  id,
  primaryText,
  openListItems,
  toggleExpand,
  toggleSingleEntry,
  toggleIncludingChildren,
  selectable,
  nestedItems,
  itemOptions: {zoomable, report},
  centerMapOnMeter,
  medium = Medium.unknown,
}: Props) => {
  const onToggleExpand = nestedItems ? () => toggleExpand(id) : () => null;

  const props: ListItemProps = {};
  if (nestedItems) {
    props.onClick = () => toggleIncludingChildren(id);
  }

  const zoomInOn = (ev: React.SyntheticEvent<{}>) => {
    ev.stopPropagation();
    centerMapOnMeter(id);
  };

  const addMeterToReport = (ev: React.SyntheticEvent<{}>) => {
    ev.stopPropagation();
    addToReport(id);
  };

  const selectedId: Maybe<uuid> = Maybe.maybe(id);

  const content = !nestedItems
    ? (
      <RowCenter className="space-between">
        <RowCenter className="first-uppercase" style={listItemStyleWithActions.textStyle}>
          <OpenDialogInfoButton
            label={primaryText}
            autoScrollBodyContent={true}
            labelStyle={labelStyle}
          >
            <MeterDetailsContainer selectedId={selectedId}/>
          </OpenDialogInfoButton>
        </RowCenter>
        <Row style={iconRowStyle}>
          {zoomable && <ZoomButton onClick={zoomInOn}/>}
          {report && medium && <MediumButton onClick={addMeterToReport} medium={medium}/>}
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
      {...props}
      className="TreeListItem first-uppercase"
      primaryText={content}
      key={id}
      innerDivStyle={sideBarStyles.padding}
      initiallyOpen={openListItems.has(id)}
      nestedListStyle={nestedListItemStyle}
      nestedItems={nestedItems}
      onNestedListToggle={onToggleExpand}
      selectable={selectable}
    />
  );
};
