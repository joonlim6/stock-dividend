package com.dayone.service;

import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if(exists) {
            throw new RuntimeException("ticker already exists -> " + ticker);
        }
        return storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    public Company storeCompanyAndDividend(String ticker) {
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));

        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
            .map(e -> new DividendEntity(companyEntity.getId(), e))
            .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }
}
