import * as React from 'react';
import {menuItemStyle, nestedListItemStyle, sideBarInnerDivStyle} from '../../../../app/themes';
import {MediumButton} from '../../../../components/buttons/MediumButton';
import {OpenDialogInfoButton} from '../../../../components/dialog/OpenDialogInfoButton';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {FirstUpper} from '../../../../components/texts/Texts';
import {MeterDetailsContainer} from '../../../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../../../helpers/Maybe';
import {orUnknown} from '../../../../helpers/translations';
import {firstUpper} from '../../../../services/translationService';
import {NormalizedSelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {Medium} from '../../../../state/ui/graph/measurement/measurementModels';
import {OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import '../../../report/components/indicators/ReportIndicatorWidget.scss';
import {SelectableListItem} from './SelectableListItem';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface RenderProps {
  addToReport: OnClickWithId;
  id: uuid;
  openListItems: Set<uuid>;
  selectionTree: NormalizedSelectionTree;
  toggleExpand: OnClickWithId;
  toggleIncludingChildren: OnClick;
  toggleSingleEntry: OnClickWithId;
  itemOptions: ItemOptions;
}

export interface ItemOptions {
  hasInfoButton?: boolean;
  zoomable?: boolean;
  report?: boolean;
}

const makeNestedItems = (
  props: RenderProps,
  ids: uuid[],
  key: string,
  onRenderItem: (props: RenderProps) => React.ReactElement<ListItemProps>,
): Array<React.ReactElement<ListItemProps>> => {
  if (props.openListItems.has(props.id)) {
    return ids.sort().map((id: uuid) => onRenderItem({...props, id}));
  } else {
    return [<React.Fragment key={key}/>];
  }
};

export const renderSelectionTreeCity = (props: RenderProps) => {
  const {id, selectionTree: {entities: {cities}}, ...other} = props;
  const city = cities[id];

  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: orUnknown(city.name),
    nestedItems: makeNestedItems(props, city.addresses, 'city123', renderSelectionTreeAddresses),
  });
};

const renderSelectionTreeAddresses = (props: RenderProps) => {
  const {id, selectionTree: {entities: {addresses}}, ...other} = props;
  const address = addresses[id];

  return renderSelectableListItem({
    ...other,
    id,
    selectable: true,
    primaryText: orUnknown(address.name),
    nestedItems: makeNestedItems(props, address.meters, 'address123', renderSelectionTreeMeters),
  });
};

const renderSelectionTreeMeters = ({id, selectionTree: {entities: {meters}}, ...other}: RenderProps) => {
  const {name: primaryText, medium} = meters[id];
  return renderSelectableListItem({
    ...other,
    id,
    selectable: false,
    primaryText,
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
  medium?: Medium;
}

const labelStyle: React.CSSProperties = {
  marginLeft: 4,
  paddingLeft: 0,
  paddingRight: 0,
  cursor: 'normal',
  width: 126,
  overflow: 'hidden',
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
  medium = Medium.unknown,
}: Props) => {
  const onToggleExpand = nestedItems ? () => toggleExpand(id) : () => null;

  const props: ListItemProps = {};
  if (nestedItems) {
    props.onClick = () => toggleIncludingChildren(id);
  }

  const addMeterToReport = (ev: React.SyntheticEvent<{}>) => {
    ev.stopPropagation();
    addToReport(id);
  };

  const selectedId: Maybe<uuid> = Maybe.maybe(id);

  const content = !nestedItems
    ? (
      <RowCenter className="space-between">
        <RowCenter className="first-uppercase" style={menuItemStyle.textStyle}>
          <OpenDialogInfoButton
            label={primaryText}
            title={primaryText}
            autoScrollBodyContent={true}
            labelStyle={labelStyle}
          >
            <MeterDetailsContainer selectedId={selectedId}/>
          </OpenDialogInfoButton>
        </RowCenter>
        <Row style={iconRowStyle}>
          {report && medium && <MediumButton onClick={addMeterToReport} medium={medium}/>}
        </Row>
      </RowCenter>
    )
    : (
      <FirstUpper style={menuItemStyle.textStyle} title={firstUpper(primaryText)}>
        {primaryText}
      </FirstUpper>
    );

  return (
    <SelectableListItem
      {...props}
      className="TreeListItem first-uppercase"
      primaryText={content}
      key={id}
      innerDivStyle={sideBarInnerDivStyle}
      initiallyOpen={openListItems.has(id)}
      nestedListStyle={nestedListItemStyle}
      nestedItems={nestedItems}
      onNestedListToggle={onToggleExpand}
      selectable={selectable}
    />
  );
};
