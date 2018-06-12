import FlatButton from 'material-ui/FlatButton';
import NavigationChevronLeft from 'material-ui/svg-icons/navigation/chevron-left';
import NavigationChevronRight from 'material-ui/svg-icons/navigation/chevron-right';
import * as React from 'react';
import {idGenerator} from '../../helpers/idGenerator';
import {Pagination} from '../../state/ui/pagination/paginationModels';
import {RowCenter} from '../layouts/row/Row';
import {PageNumberButton} from './PageNumberButton';
import './PaginationControl.scss';
import {PageLink, pageLinks} from './paginationHelper';

type PageElements = Array<React.ReactElement<FlatButton | HTMLSpanElement>>;

interface PageNumberProps {
  current: number;
  total: number;
  changePage: (page: number) => void;
}

const visibilityProximity: number = 2;

const renderPageNumberButtons = ({total, current, changePage}: PageNumberProps): PageElements => {
  const links: PageLink[] = pageLinks(current, total, visibilityProximity);
  return links.map((link: PageLink) => {
    if (link === 'dot') {
      const key: string = `pagination-${idGenerator.uuid()}`;
      return <span key={key}>...</span>;
    }

    const pageOneIndexed: number = link[0] + 1;
    const key: string = `pagination-${pageOneIndexed}`;
    if (link[1] === 'current') {
      return <PageNumberButton disabled={true} key={key} page={pageOneIndexed}/>;
    }

    const onClick = () => changePage(pageOneIndexed - 1);
    return <PageNumberButton onClick={onClick} key={key} page={pageOneIndexed}/>;
  });
};

const iconArrowStyle = {marginTop: 8};

interface Props {
  pagination: Pagination;
  changePage: (page: number) => void;
}

export const PaginationControl =
  ({pagination: {page, totalPages}, changePage}: Props) => {

    if (totalPages <= 1) {
      return null;
    }

    const noPrev = page === 0;
    const noNext = page + 1 >= totalPages;

    const changePagePrev = noPrev ? () => void(0) : () => changePage(page - 1);
    const changePageNext = noNext ? () => void(0) : () => changePage(page + 1);

    const pageNumberButtons = renderPageNumberButtons({
      current: page,
      total: totalPages,
      changePage,
    });

    return (
      <RowCenter>
        <RowCenter className="PaginationControl">
          <FlatButton disabled={noPrev} onClick={changePagePrev} className="PageNumber-arrow">
            <NavigationChevronLeft style={iconArrowStyle}/>
          </FlatButton>
          {pageNumberButtons}
          <FlatButton disabled={noNext} onClick={changePageNext} className="PageNumber-arrow">
            <NavigationChevronRight style={iconArrowStyle}/>
          </FlatButton>
        </RowCenter>
      </RowCenter>
    );
  };
