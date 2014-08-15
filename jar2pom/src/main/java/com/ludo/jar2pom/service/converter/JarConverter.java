package com.ludo.jar2pom.service.converter;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.ludo.jar2pom.core.file.FileJarVisitor;
import com.ludo.jar2pom.core.model.Descriptor;
import com.ludo.jar2pom.core.remote.DescriptorStrategy;
import com.ludo.jar2pom.model.Arguments;
import com.ludo.jar2pom.service.output.OutputWriter;

public class JarConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(JarConverter.class);

    private final DescriptorStrategy descriptorStrategy;

    private final OutputWriter outputWriter;

    public JarConverter(final DescriptorStrategy descriptorStrategy, final OutputWriter outputWriter) {
        super();
        this.descriptorStrategy = descriptorStrategy;
        this.outputWriter = outputWriter;
    }

    @Override
    public final void process(final Arguments arguments) throws IOException {
        Objects.requireNonNull(arguments, "Arguments cannot be null.");
        LOG.debug("Arguments: {}", arguments);

        // get descriptors
        final Path input = arguments.getInput();
        final String customHost = arguments.getCustomHost();
        final boolean recursive = arguments.isRecursive();
        final List<Descriptor> descriptors = this.getDescriptors(input, customHost, recursive);

        // write output file
        final Path output = arguments.getOutput();
        this.outputWriter.writeOutputFile(descriptors, output);
    }

    protected final List<Descriptor> getDescriptors(final Path input, final String customHost, final boolean recursive) throws IOException {
        Objects.requireNonNull(input, "Input path cannot be null.");

        // create file visitor
        final FileJarVisitor fileVisitor = new FileJarVisitor(this.descriptorStrategy, customHost);

        // browse file and create Descriptor
        if (recursive) {
            Files.walkFileTree(input, fileVisitor);
        } else {
            final Set<FileVisitOption> options = Sets.newHashSet(FileVisitOption.FOLLOW_LINKS);
            Files.walkFileTree(input, options, 1, fileVisitor);
        }

        // get all Descriptor
        return fileVisitor.getDescriptors();
    }

}
