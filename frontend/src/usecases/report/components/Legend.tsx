import * as React from 'react';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonLinkBlue} from '../../../components/buttons/ButtonLink';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconIndicator} from '../../../components/icons/IconIndicator';
import {Row, RowMiddle} from '../../../components/layouts/row/Row';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {isDefined} from '../../../helpers/commonUtils';
import {orUnknown} from '../../../helpers/translations';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {Medium} from '../../../state/ui/graph/measurement/measurementModels';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {LegendItem} from '../reportModels';
import './Legend.scss';

export interface LegendProps {
  hiddenLines: uuid[];
  toggleLine: OnClick;
  clearSelectedListItems: OnClick;
  deleteItem: OnClickWithId;
  legendItems: Normalized<LegendItem>;
}

const iconIndicatorStyle: React.CSSProperties = {
  display: 'table',
  width: 24,
  height: 24,
};

const renderFacility = ({facility}: LegendItem) => facility ? orUnknown(facility) : '-';

const renderAddress = ({address}: LegendItem) => address ? orUnknown(address) : '-';

const renderCity = ({city}: LegendItem) => city ? orUnknown(city) : '-';

const renderMedium = ({medium}: LegendItem) =>
  Array.isArray(medium)
    ? medium.map((singleMedium: Medium, index) => (
      <IconIndicator
        key={`${singleMedium}-${index}`}
        medium={singleMedium}
        style={iconIndicatorStyle}
      />
    ))
    : <IconIndicator medium={medium} style={iconIndicatorStyle}/>;

export const Legend = ({
  clearSelectedListItems,
  hiddenLines,
  legendItems: {entities: {lines}, result},
  toggleLine,
  deleteItem
}: LegendProps) => {
  const removeAllButtonLink =
    <ButtonLinkBlue onClick={clearSelectedListItems}>{translate('remove all')}</ButtonLinkBlue>;

  const renderDeleteButton = ({id}: LegendItem) => {
    const checked = isDefined(hiddenLines.find((it) => it === id));
    const onDeleteItem = () => deleteItem(id);
    return (
      <RowMiddle>
        <Row style={{marginRight: 8}}>
          <ButtonVisibility key={`checked-${id}-${checked}`} onClick={toggleLine} id={id} checked={checked}/>
        </Row>
        <Row>
          <ButtonDelete onClick={onDeleteItem}/>
        </Row>
      </RowMiddle>
    );
  };

  return (
    <Row className="Legend">
      <Table result={result} entities={lines}>
        <TableColumn
          header={<TableHead className="first">{translate('facility')}</TableHead>}
          cellClassName={'first first-uppercase'}
          renderCell={renderFacility}
        />
        <TableColumn
          header={<TableHead>{firstUpperTranslated('city')}</TableHead>}
          renderCell={renderCity}
        />
        <TableColumn
          header={<TableHead>{translate('address')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderAddress}
        />
        <TableColumn
          header={<TableHead>{translate('medium')}</TableHead>}
          cellClassName={'icon'}
          renderCell={renderMedium}
        />
        <TableColumn
          header={<TableHead className="Link">{removeAllButtonLink}</TableHead>}
          cellClassName="icon"
          renderCell={renderDeleteButton}
        />
      </Table>
    </Row>
  );
};
